import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class KafkaHttpService {
  protected resourceUrl = this.appService.getEndpointFor('api/redpanda-kafka');

  constructor(private readonly httpClient: HttpClient, private readonly appService: ApplicationConfigService) {}

  publish(): Observable<any> {
    return this.httpClient.get(`${this.resourceUrl}/publish`);
  }
}
