import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../../core/services/product.service';
import { CategoryService } from '../../core/services/category.service';
import { Category, Product } from '../../core/models/models';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  isEditMode = false;
  productId: number | null = null;
  productForm!: FormGroup;
  categories: Category[] = [];
  loading = false;
  submitting = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();

    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (idParam) {
        this.isEditMode = true;
        this.productId = +idParam;
        this.loadProductDetails(this.productId);
      }
    });
  }

  private initForm(): void {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
      sku: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9_-]+$/), Validators.minLength(3)]],
      description: [''],
      categoryId: ['', [Validators.required]],
      vendorName: [''],
      price: [null, [Validators.required, Validators.min(0.01)]],
      costPrice: [null, [Validators.required, Validators.min(0.01)]],
      currentStock: [0, [Validators.required, Validators.min(0)]],
      minStockThreshold: [10, [Validators.required, Validators.min(0)]],
      reorderQuantity: [50, [Validators.required, Validators.min(1)]],
      unit: ['pcs', [Validators.required]]
    });
  }

  private loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (data) => (this.categories = data),
      error: () => (this.errorMessage = 'Failed to load categories.')
    });
  }

  private loadProductDetails(id: number): void {
    this.loading = true;
    this.productService.getProductById(id).subscribe({
      next: (product: Product) => {
        this.loading = false;
        this.productForm.patchValue({
          name: product.name,
          sku: product.sku,
          description: product.description,
          categoryId: product.categoryId,
          vendorName: product.vendorName,
          price: product.price,
          costPrice: product.costPrice,
          currentStock: product.currentStock,
          minStockThreshold: product.minStockThreshold,
          reorderQuantity: product.reorderQuantity,
          unit: product.unit || 'pcs'
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to load product details.';
      }
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const payload = {
      ...this.productForm.value,
      sku: this.productForm.value.sku.toUpperCase()
    };

    if (this.isEditMode && this.productId) {
      this.productService.updateProduct(this.productId, payload).subscribe({
        next: () => {
          this.submitting = false;
          this.successMessage = 'Product updated successfully! Redirecting...';
          setTimeout(() => this.router.navigate(['/products']), 1000);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err.error?.message || 'Failed to update product.';
        }
      });
    } else {
      this.productService.createProduct(payload).subscribe({
        next: () => {
          this.submitting = false;
          this.successMessage = 'Product created successfully! Redirecting...';
          setTimeout(() => this.router.navigate(['/products']), 1000);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err.error?.message || 'Failed to create product.';
        }
      });
    }
  }
}
