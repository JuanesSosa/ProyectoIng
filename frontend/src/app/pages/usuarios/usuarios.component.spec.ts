import { validarUsuario } from './usuarios.component';

describe('validarUsuario', () => {
  const base = { nombre: 'Ana', correo: 'ana@x.com', password: 'Clave1234', rol: 'DOCENTE' as const, estudianteId: null, activo: true };

  it('acepta un usuario completo', () => {
    expect(validarUsuario(base, true)).toBe('');
  });
  it('explica qué falta', () => {
    expect(validarUsuario({ ...base, nombre: ' ' }, true)).toContain('nombre');
    expect(validarUsuario({ ...base, correo: 'ana' }, true)).toContain('correo');
    expect(validarUsuario({ ...base, password: '' }, true)).toContain('contraseña');
    expect(validarUsuario({ ...base, password: '123' }, true)).toContain('8 caracteres');
    expect(validarUsuario({ ...base, rol: 'ESTUDIANTE' }, true)).toContain('estudiante');
  });
  it('al editar permite dejar la contraseña vacía', () => {
    expect(validarUsuario({ ...base, password: '' }, false)).toBe('');
  });
});
