/**
 * Modelo/interface que representa un producto en el frontend.
 * Debe coincidir con los campos que devuelve la API del backend.
 * "id" es opcional porque al crear un producto nuevo aún no tiene id (lo asigna el backend).
 */
export interface Product {
  id?: number;
  name: string;
  description: string;
  price: number;
  stock: number;
}
