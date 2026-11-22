/** Utilidades para entregas (archivos): tamaño legible, validación previa y descarga de un Blob. */

export const TAMANO_MAXIMO_BYTES = 10 * 1024 * 1024;
export const EXTENSIONES_PERMITIDAS = ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'zip', 'png', 'jpg', 'jpeg'];

/** 1536 → "1.5 KB". */
export function formatoTamano(bytes: number): string {
  if (bytes < 1024) {
    return `${bytes} B`;
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

/** Comprueba tipo y tamaño antes de subir (el servidor vuelve a validar). Devuelve el error o ''. */
export function validarArchivo(nombre: string, tamano: number): string {
  const punto = nombre.lastIndexOf('.');
  const extension = punto < 0 ? '' : nombre.substring(punto + 1).toLowerCase();
  if (!EXTENSIONES_PERMITIDAS.includes(extension)) {
    return `Tipo de archivo no permitido. Se aceptan: ${EXTENSIONES_PERMITIDAS.join(', ')}.`;
  }
  if (tamano <= 0) {
    return 'El archivo está vacío.';
  }
  if (tamano > TAMANO_MAXIMO_BYTES) {
    return 'El archivo supera el tamaño máximo de 10 MB.';
  }
  return '';
}

/** Dispara la descarga de un Blob en el navegador. */
export function guardarBlob(blob: Blob, nombre: string): void {
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombre;
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  URL.revokeObjectURL(url);
}
