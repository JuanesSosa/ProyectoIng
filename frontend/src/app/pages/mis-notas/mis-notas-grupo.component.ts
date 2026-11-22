import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ActividadResumen, Entrega, ResumenGrupo } from '../../core/api.models';
import { formatoTamano, guardarBlob, validarArchivo } from '../../core/archivos';
import { mensajeError } from '../../core/errores';
import { NOTA_MINIMA_APROBATORIA, notaNecesaria } from '../../core/promedio';
import { ActividadService } from '../../services/actividad.service';
import { GrupoService } from '../../services/grupo.service';

/**
 * Nota del curso para el estudiante: nota acumulada y promedio, avance de la asignatura, cada actividad con su
 * rúbrica (nota por criterio, con barras) y la subida de archivos de su entrega por actividad.
 */
@Component({
  selector: 'app-mis-notas-grupo',
  standalone: true,
  imports: [RouterLink],
  template: `
    <p><a routerLink="/mis-notas">← Mis asignaturas</a></p>
    @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
    @if (resumen(); as r) {
      <h1>{{ r.asignaturaNombre }} · {{ r.grupoNombre }} <span class="suave">({{ r.periodo }})</span></h1>
      <p class="suave">{{ r.estudianteNombre }} · Código: {{ r.estudianteCodigo ?? '—' }} · Correo: {{ r.estudianteCorreo ?? '—' }}</p>

      <div class="fila">
        <div class="tarjeta dato">
          <span class="suave">Nota acumulada</span>
          <strong [class.aprobado]="r.avance === 100 && r.acumulado >= minimo" [class.reprobado]="r.avance === 100 && r.acumulado < minimo">{{ r.acumulado }}</strong>
          <span class="suave">puntos sobre 5.0</span>
          <div class="barra" role="img" [attr.aria-label]="'Acumulado ' + r.acumulado + ' de 5.0'">
            <div class="relleno" [style.width.%]="(r.acumulado / 5) * 100"></div>
            <div class="marca" [style.left.%]="(minimo / 5) * 100" title="Nota mínima para aprobar"></div>
          </div>
        </div>
        <div class="tarjeta dato">
          <span class="suave">Promedio de lo evaluado</span>
          <strong [class.reprobado]="r.promedio !== null && r.promedio < minimo">{{ r.promedio ?? '–' }}</strong>
          <span class="suave">nota de lo calificado hasta ahora</span>
        </div>
        <div class="tarjeta dato">
          <span class="suave">Avance de la asignatura</span>
          <strong>{{ r.avance }}%</strong>
          <span class="suave">del curso ya evaluado</span>
          <div class="barra" role="img" [attr.aria-label]="'Avance ' + r.avance + ' por ciento'">
            <div class="relleno avance" [style.width.%]="r.avance"></div>
          </div>
        </div>
      </div>

      <p class="estado" role="status">{{ mensajeNecesaria() }}</p>

      @for (a of r.actividades; track a.actividadId) {
        <div class="tarjeta">
          <div class="cabecera">
            <h2>{{ a.nombre }} <span class="suave">({{ a.peso }}% del curso)</span></h2>
            <strong [class.reprobado]="a.nota !== null && a.nota < minimo" [class.aprobado]="a.nota !== null && a.nota >= minimo">{{ a.nota ?? 'Sin calificar' }}</strong>
          </div>
          @if (a.criterios.length === 0) {
            <p class="suave">La rúbrica todavía no está definida.</p>
          } @else {
            <div class="scroll">
              <table>
                <thead><tr><th>Criterio</th><th class="num">Peso</th><th>Nota (0.0 – 5.0)</th></tr></thead>
                <tbody>
                  @for (c of a.criterios; track c.criterioId) {
                    <tr>
                      <td>{{ c.nombre }}</td>
                      <td class="num">{{ c.peso }}%</td>
                      <td>
                        <div class="celda-nota">
                          <div class="barra pequena" role="img" [attr.aria-label]="c.valor === null ? 'Sin calificar' : 'Nota ' + c.valor">
                            <div class="relleno" [class.mala]="c.valor !== null && c.valor < minimo" [style.width.%]="((c.valor ?? 0) / 5) * 100"></div>
                          </div>
                          <span [class.reprobado]="c.valor !== null && c.valor < minimo">{{ c.valor ?? 'pendiente' }}</span>
                        </div>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }

          <h3>Mi entrega</h3>
          @if (errores()[a.actividadId]) { <p class="error" role="alert">{{ errores()[a.actividadId] }}</p> }
          @if (entregasDe(a.actividadId).length === 0) {
            <p class="suave">Todavía no has subido archivos.</p>
          } @else {
            <ul class="entregas">
              @for (e of entregasDe(a.actividadId); track e.id) {
                <li>
                  <span>{{ e.nombre }}</span>
                  <span class="suave">{{ tamano(e.tamano) }} · {{ e.fecha.substring(0, 10) }}</span>
                  <button type="button" class="btn btn-secundario btn-pequeno" (click)="descargar(e)">Descargar</button>
                  <button type="button" class="btn btn-peligro btn-pequeno" (click)="borrar(a, e)">Borrar</button>
                </li>
              }
            </ul>
          }
          <div class="fila subida">
            <input type="file" [id]="'arch' + a.actividadId" [attr.aria-label]="'Archivo para ' + a.nombre"
                   accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.png,.jpg,.jpeg"
                   (change)="subir(a, $event)" [disabled]="subiendo() === a.actividadId" />
            @if (subiendo() === a.actividadId) { <span class="suave">Subiendo…</span> }
          </div>
          <p class="suave pequeno">Hasta 5 archivos por actividad, máximo 10 MB cada uno (pdf, doc, docx, ppt, pptx, xls, xlsx, txt, zip, png, jpg).</p>
        </div>
      } @empty {
        <p class="suave">Este grupo todavía no tiene actividades.</p>
      }
      @if (r.actividades.length > 0 && r.pesoActividades !== 100) {
        <p class="suave">Las actividades definidas suman {{ r.pesoActividades }}% de la nota final.</p>
      }
    }
  `,
  styles: [
    `
      h2 { margin: 0; font-size: 1.05rem; }
      h3 { margin: 1rem 0 0.4rem; font-size: 0.95rem; }
      .cabecera { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
      .dato { flex: 1; min-width: 200px; display: flex; flex-direction: column; gap: 0.2rem; }
      .dato strong { font-size: 2rem; }
      .barra { position: relative; height: 10px; border-radius: 5px; background: #e5e7eb; margin-top: 0.4rem; overflow: hidden; }
      .barra.pequena { height: 8px; flex: 1; min-width: 80px; margin: 0; }
      .relleno { height: 100%; background: var(--color-primario); }
      .relleno.avance { background: #0d9488; }
      .relleno.mala { background: #dc2626; }
      .marca { position: absolute; top: 0; bottom: 0; width: 2px; background: #111827; }
      .celda-nota { display: flex; align-items: center; gap: 0.6rem; }
      .estado { margin: 0.5rem 0 1rem; font-weight: 600; }
      .entregas { list-style: none; padding: 0; margin: 0 0 0.5rem; display: grid; gap: 0.4rem; }
      .entregas li { display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap; }
      .subida { align-items: center; }
      .pequeno { font-size: 0.8rem; margin: 0.3rem 0 0; }
      a { color: var(--color-primario); }
    `,
  ],
})
export class MisNotasGrupoComponent implements OnInit {
  private readonly ruta = inject(ActivatedRoute);
  private readonly grupos = inject(GrupoService);
  private readonly actividadService = inject(ActividadService);
  protected readonly minimo = NOTA_MINIMA_APROBATORIA;
  protected readonly tamano = formatoTamano;

  readonly resumen = signal<ResumenGrupo | null>(null);
  readonly error = signal('');
  readonly entregas = signal<Partial<Record<number, Entrega[]>>>({});
  readonly errores = signal<Record<number, string>>({});
  readonly subiendo = signal<number | null>(null);

  /** Mensaje según lo que le falta al estudiante para llegar a la nota mínima. */
  readonly mensajeNecesaria = computed(() => {
    const r = this.resumen();
    if (!r || r.actividades.length === 0) {
      return '';
    }
    if (r.avance === 0) {
      return 'Todavía no hay notas registradas.';
    }
    const necesaria = notaNecesaria(r.acumulado, r.avance);
    if (necesaria === null) {
      return r.acumulado >= this.minimo
        ? `Curso evaluado por completo: nota final ${r.acumulado}. ¡Aprobaste!`
        : `Curso evaluado por completo: nota final ${r.acumulado}. No alcanzaste la nota mínima (${this.minimo}).`;
    }
    if (necesaria <= 0) {
      return `Ya alcanzaste la nota mínima de ${this.minimo}. Lo que falta solo suma.`;
    }
    if (necesaria > 5) {
      return `Con lo que falta por evaluar ya no es posible llegar a ${this.minimo}.`;
    }
    return `Para llegar a ${this.minimo} necesitas un promedio de ${necesaria} en el ${100 - r.avance}% que falta por evaluar.`;
  });

  private grupoId = 0;

  ngOnInit(): void {
    this.grupoId = Number(this.ruta.snapshot.paramMap.get('id'));
    this.grupos.resumen(this.grupoId).subscribe({
      next: (r) => {
        this.resumen.set(r);
        r.actividades.forEach((a) => this.cargarEntregas(a.actividadId));
      },
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar tus notas.')),
    });
  }

  protected entregasDe(actividadId: number): Entrega[] {
    return this.entregas()[actividadId] ?? [];
  }

  private cargarEntregas(actividadId: number): void {
    this.actividadService.entregas(actividadId).subscribe({
      next: (lista) => this.entregas.update((e) => ({ ...e, [actividadId]: lista })),
      error: (err) => this.ponerError(actividadId, mensajeError(err, 'No se pudieron cargar tus entregas.')),
    });
  }

  private ponerError(actividadId: number, mensaje: string): void {
    this.errores.update((e) => ({ ...e, [actividadId]: mensaje }));
  }

  subir(actividad: ActividadResumen, evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) {
      return;
    }
    this.ponerError(actividad.actividadId, '');
    const problema = validarArchivo(archivo.name, archivo.size);
    if (problema) {
      this.ponerError(actividad.actividadId, problema);
      input.value = '';
      return;
    }
    this.subiendo.set(actividad.actividadId);
    this.actividadService.subirEntrega(actividad.actividadId, archivo).subscribe({
      next: () => {
        this.subiendo.set(null);
        input.value = '';
        this.cargarEntregas(actividad.actividadId);
      },
      error: (err) => {
        this.subiendo.set(null);
        input.value = '';
        this.ponerError(actividad.actividadId, mensajeError(err, 'No se pudo subir el archivo.'));
      },
    });
  }

  descargar(entrega: Entrega): void {
    this.ponerError(entrega.actividadId, '');
    this.actividadService.descargarEntrega(entrega.id).subscribe({
      next: (blob) => guardarBlob(blob, entrega.nombre),
      error: (err) => this.ponerError(entrega.actividadId, mensajeError(err, 'No se pudo descargar el archivo.')),
    });
  }

  borrar(actividad: ActividadResumen, entrega: Entrega): void {
    if (!confirm(`¿Borrar el archivo ${entrega.nombre}?`)) {
      return;
    }
    this.actividadService.eliminarEntrega(entrega.id).subscribe({
      next: () => this.cargarEntregas(actividad.actividadId),
      error: (err) => this.ponerError(actividad.actividadId, mensajeError(err, 'No se pudo borrar el archivo.')),
    });
  }
}
