import { formatoTamano, validarArchivo } from './archivos';

describe('archivos', () => {
  it('formatea el tamaño', () => {
    expect(formatoTamano(500)).toBe('500 B');
    expect(formatoTamano(1536)).toBe('1.5 KB');
    expect(formatoTamano(5 * 1024 * 1024)).toBe('5.0 MB');
  });
  it('acepta tipos permitidos y rechaza el resto', () => {
    expect(validarArchivo('informe.PDF', 1000)).toBe('');
    expect(validarArchivo('virus.exe', 1000)).toContain('no permitido');
    expect(validarArchivo('sinextension', 1000)).toContain('no permitido');
  });
  it('rechaza archivos vacíos o de más de 10 MB', () => {
    expect(validarArchivo('a.txt', 0)).toContain('vacío');
    expect(validarArchivo('a.zip', 11 * 1024 * 1024)).toContain('10 MB');
  });
});
