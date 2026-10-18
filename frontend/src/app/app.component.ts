import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/** Componente raíz: solo muestra la pantalla que corresponda a la ruta (login o shell con menú). */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
})
export class AppComponent {}
