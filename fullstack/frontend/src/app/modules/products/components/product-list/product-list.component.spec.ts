import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProductListComponent } from './product-list.component';
import { environment } from '../../../../../environments/environment';

describe('ProductListComponent', () => {
  let fixture: ComponentFixture<ProductListComponent>;
  let component: ProductListComponent;
  let http: HttpTestingController;
  const url = `${environment.apiUrl}/products`;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductListComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges(); // dispara ngOnInit -> GET inicial
    http.expectOne(url).flush([]);
  });

  afterEach(() => http.verify());

  it('no envía nada si el formulario es inválido', () => {
    component.form.setValue({ name: '', description: '', price: null, stock: null });
    component.save();
    http.expectNone(url);
    expect(component.form.touched).toBeTrue();
  });

  it('crea un producto válido con POST y recarga la lista', () => {
    component.form.setValue({ name: ' Mouse ', description: '', price: 10, stock: 3 });
    component.save();

    const req = http.expectOne({ method: 'POST', url });
    expect(req.request.body.name).toBe('Mouse');
    req.flush({ id: 1, name: 'Mouse', description: '', price: 10, stock: 3 });

    http.expectOne({ method: 'GET', url }).flush([]);
  });

  it('en modo edición usa PUT con el id del producto', () => {
    component.edit({ id: 5, name: 'A', description: '', price: 2, stock: 1 });
    component.save();

    const req = http.expectOne({ method: 'PUT', url: `${url}/5` });
    req.flush({ id: 5, name: 'A', description: '', price: 2, stock: 1 });
    http.expectOne({ method: 'GET', url }).flush([]);
    expect(component.editingId).toBeNull();
  });

  it('muestra los errores por campo que devuelve el backend', () => {
    component.form.setValue({ name: 'X', description: '', price: 1, stock: 1 });
    component.save();

    http.expectOne({ method: 'POST', url }).flush(
      { errors: { price: 'El precio debe ser mayor que 0' } },
      { status: 400, statusText: 'Bad Request' }
    );
    expect(component.actionError).toContain('El precio debe ser mayor que 0');
  });
});
