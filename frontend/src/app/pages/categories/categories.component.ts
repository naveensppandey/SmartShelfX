import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../core/services/category.service';
import { Category } from '../../core/models/models';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.scss']
})
export class CategoriesComponent implements OnInit {
  categories: Category[] = [];
  loading = true;
  errorMessage = '';
  successMessage = '';

  // Modal State
  showModal = false;
  isEditMode = false;
  currentId: number | null = null;
  categoryName = '';
  categoryDescription = '';
  submitting = false;

  // Deletion Modal
  categoryToDelete: Category | null = null;
  deleting = false;

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.categoryService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to load categories.';
      }
    });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.currentId = null;
    this.categoryName = '';
    this.categoryDescription = '';
    this.errorMessage = '';
    this.showModal = true;
  }

  openEditModal(category: Category): void {
    this.isEditMode = true;
    this.currentId = category.id;
    this.categoryName = category.name;
    this.categoryDescription = category.description || '';
    this.errorMessage = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  onSubmit(): void {
    if (!this.categoryName.trim()) {
      this.errorMessage = 'Category name is required.';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const payload = {
      name: this.categoryName.trim(),
      description: this.categoryDescription.trim()
    };

    if (this.isEditMode && this.currentId) {
      this.categoryService.updateCategory(this.currentId, payload).subscribe({
        next: () => {
          this.submitting = false;
          this.showModal = false;
          this.successMessage = 'Category updated successfully!';
          this.loadCategories();
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err.error?.message || 'Failed to update category.';
        }
      });
    } else {
      this.categoryService.createCategory(payload).subscribe({
        next: () => {
          this.submitting = false;
          this.showModal = false;
          this.successMessage = 'Category created successfully!';
          this.loadCategories();
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err.error?.message || 'Failed to create category.';
        }
      });
    }
  }

  confirmDelete(cat: Category): void {
    this.categoryToDelete = cat;
    this.errorMessage = '';
  }

  cancelDelete(): void {
    this.categoryToDelete = null;
  }

  executeDelete(): void {
    if (!this.categoryToDelete) return;

    this.deleting = true;
    this.categoryService.deleteCategory(this.categoryToDelete.id).subscribe({
      next: () => {
        this.deleting = false;
        this.successMessage = `Category '${this.categoryToDelete?.name}' deleted successfully.`;
        this.categoryToDelete = null;
        this.loadCategories();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        this.deleting = false;
        this.errorMessage = err.error?.message || 'Failed to delete category.';
        this.categoryToDelete = null;
      }
    });
  }
}
