import { Link } from 'react-router-dom';
import type { ProductDto } from '../types/api';
import { getCategoryName } from '../utils/catalog';
import { formatCurrency } from '../utils/format';
import { getProductImageUri } from '../utils/productImage';

interface ProductCardProps {
  product: ProductDto;
  onAddToCart: (productId: number) => Promise<void>;
  disabled?: boolean;
}

export function ProductCard({ product, onAddToCart, disabled = false }: ProductCardProps) {
  return (
    <article className="flex h-full flex-col rounded-xl border border-stone-200 bg-white p-4 shadow-sm">
      <Link to={`/products/${product.id}`} className="group">
        <div className="mb-3 h-36 overflow-hidden rounded-lg">
          <img
            src={getProductImageUri(product.name, product.categoryId)}
            alt={product.name}
            className="h-full w-full object-cover"
          />
        </div>
        <h3 className="line-clamp-1 text-base font-semibold text-stone-900 transition-colors group-hover:text-indigo-600">
          {product.name}
        </h3>
      </Link>
      <p className="mt-2 line-clamp-2 text-sm text-stone-500">{product.description}</p>
      <div className="mt-auto pt-4">
        <p className="text-xs font-medium uppercase tracking-wide text-stone-400">
          {getCategoryName(product.categoryId)}
        </p>
        <div className="mt-2 flex items-center justify-between">
          <p className="text-lg font-bold text-stone-900">{formatCurrency(product.price)}</p>
          <button
            type="button"
            onClick={() => onAddToCart(product.id)}
            disabled={disabled}
            className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-stone-300"
          >
            Add
          </button>
        </div>
      </div>
    </article>
  );
}
