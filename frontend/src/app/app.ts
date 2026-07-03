import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

export interface Item {
  id: number;
  name: string;
}

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private readonly http = inject(HttpClient);

  protected readonly items = signal<Item[]>([]);
  protected readonly name = signal('');

  constructor() {
    this.load();
  }

  protected load(): void {
    this.http.get<Item[]>('/api/items').subscribe(items => this.items.set(items));
  }

  protected add(): void {
    const name = this.name().trim();
    if (!name) {
      return;
    }
    this.http.post<Item>('/api/items', { name }).subscribe(() => {
      this.name.set('');
      this.load();
    });
  }

  protected remove(item: Item): void {
    this.http.delete(`/api/items/${item.id}`).subscribe(() => this.load());
  }
}
