import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StockTransaction, StockTransactionRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private readonly apiUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  getAllTransactions(): Observable<StockTransaction[]> {
    return this.http.get<StockTransaction[]>(this.apiUrl);
  }

  getTransactionsByProduct(productId: number): Observable<StockTransaction[]> {
    return this.http.get<StockTransaction[]>(`${this.apiUrl}/product/${productId}`);
  }

  getRecentTransactions(): Observable<StockTransaction[]> {
    return this.http.get<StockTransaction[]>(`${this.apiUrl}/recent`);
  }

  recordTransaction(request: StockTransactionRequest): Observable<StockTransaction> {
    return this.http.post<StockTransaction>(this.apiUrl, request);
  }
}
