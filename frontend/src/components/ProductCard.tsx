import { Link } from 'react-router-dom';
import type { ProductDto } from '../types/api';
import { getCategoryName } from '../utils/catalog';
import { formatCurrency } from '../utils/format';

interface ProductCardProps {
  product: ProductDto;
  onAddToCart: (productId: number) => Promise<void>;
  disabled?: boolean;
}

export function ProductCard({ product, onAddToCart, disabled = false }: ProductCardProps) {
  return (
    <article className="flex h-full flex-col rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <Link to={`/products/${product.id}`} className="group">
        <div className="mb-3 flex h-36 items-center justify-center rounded-lg bg-gradient-to-br from-slate-100 to-slate-200 text-xs uppercase tracking-widest text-slate-500">
          Product Image
        </div>
        <h3 className="line-clamp-1 text-base font-semibold text-slate-900 group-hover:text-slate-700">
          {product.name}
        </h3>
      </Link>
      <p className="mt-2 line-clamp-2 text-sm text-slate-600">{product.description}</p>
      <div className="mt-auto pt-4">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
          {getCategoryName(product.categoryId)}
        </p>
        <div className="mt-2 flex items-center justify-between">
          <p className="text-lg font-bold text-slate-900">{formatCurrency(product.price)}</p>
          <button
            type="button"
            onClick={() => onAddToCart(product.id)}
            disabled={disabled}
            className="rounded-md bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            Add
          </button>
        </div>
      </div>
    </article>
  );
}
