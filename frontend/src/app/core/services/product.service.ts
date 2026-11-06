import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Product, ProductRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  getProducts(keyword?: string, categoryId?: number, stockStatus?: string): Observable<Product[]> {
    let params = new HttpParams();
    if (keyword) params = params.set('keyword', keyword);
    if (categoryId) params = params.set('categoryId', categoryId.toString());
    if (stockStatus && stockStatus !== 'ALL') params = params.set('stockStatus', stockStatus);

    return this.http.get<Product[]>(this.apiUrl, { params });
  }

  getAllProducts(): Observable<Product[]> {
    return this.getProducts();
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  getLowStockProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/low-stock`);
  }

  getOutOfStockProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/out-of-stock`);
  }

  createProduct(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, request);
  }

  updateProduct(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, request);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
