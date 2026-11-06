import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ForecastResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ForecastService {
  private readonly apiUrl = `${environment.apiUrl}/forecast`;

  constructor(private http: HttpClient) {}

  getProductForecast(productId: number, days: number = 7, leadTime: number = 3): Observable<ForecastResponse> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('leadTime', leadTime.toString());

    return this.http.get<ForecastResponse>(`${this.apiUrl}/product/${productId}`, { params });
  }

  generateCustomForecast(payload: { productId: number; daysToForecast: number; leadTimeDays: number }): Observable<ForecastResponse> {
    return this.http.post<ForecastResponse>(this.apiUrl, payload);
  }
}
