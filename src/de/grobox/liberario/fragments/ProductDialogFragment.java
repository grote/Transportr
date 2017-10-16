/*    Transportr
 *    Copyright (C) 2013 - 2016 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.liberario.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.materialdrawer.util.KeyboardUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Product;

public class ProductDialogFragment extends DialogFragment {

	public static final String TAG = ProductDialogFragment.class.getName();

	private static final String PRODUCTS = "products";
	private OnProductsChangedListener listener;
	private FastItemAdapter<ProductItem> adapter;
	private Button okButton;

	static ProductDialogFragment newInstance(EnumSet<Product> products) {
		ProductDialogFragment f = new ProductDialogFragment();

		Bundle args = new Bundle();
		args.putSerializable(PRODUCTS, products);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(Preferences.darkThemeEnabled(getActivity())) {
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SetHomeDialogTheme);
		} else {
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SetHomeDialogTheme_Light);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_product_dialog, container);

		// RecyclerView
		RecyclerView productsView = (RecyclerView) v.findViewById(R.id.productsView);
		productsView.setLayoutManager(new LinearLayoutManager(getContext()));
		adapter = new FastItemAdapter<>();
		adapter.withSelectable(true);
		productsView.setAdapter(adapter);

		// Add Products and select the ones we got
		@SuppressWarnings("unchecked")
		EnumSet<Product> products = (EnumSet<Product>) getArguments().getSerializable(PRODUCTS);
		if(products == null) throw new IllegalArgumentException("No Products. Use newInstance()");
		int i = 0;
		for(Product product : Product.ALL) {
			adapter.add(new ProductItem(product));
			if(savedInstanceState == null && products.contains(product)) {
				adapter.select(i);
			}
			i++;
		}
		adapter.withSavedInstanceState(savedInstanceState);

		// OK Button
		okButton = (Button) v.findViewById(R.id.okButton);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// call listener if set
				if(listener != null) {
					EnumSet<Product> products = getProductsFromItems(adapter.getSelectedItems());
					Preferences.setProducts(getContext(), products);
					listener.onProductsChanged(products);
				}
				getDialog().cancel();
			}
		});

		// Cancel Button
		Button cancelButton = (Button) v.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getDialog().cancel();
			}
		});
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		adapter.saveInstanceState(outState);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		KeyboardUtil.hideKeyboard(getActivity());
	}

	private EnumSet<Product> getProductsFromItems(Set<ProductItem> items) {
		EnumSet<Product> products = EnumSet.noneOf(Product.class);
		for(ProductItem item : items) {
			products.add(item.product);
		}
		return products;
	}

	void setOkEnabled(boolean enabled) {
		okButton.setEnabled(enabled);
	}

	void setOnProductsChangedListener(OnProductsChangedListener listener) {
		this.listener = listener;
	}

	interface OnProductsChangedListener {
		void onProductsChanged(EnumSet<Product> products);
	}

	class ProductItem extends AbstractItem<ProductItem, ProductItem.ViewHolder> {
		private final Product product;

		ProductItem(Product product) {
			this.product = product;
		}

		@Override
		public int getType() {
			return product.ordinal();
		}

		@Override
		public int getLayoutRes() {
			return R.layout.item_product_selectable;
		}

		@Override
		public void bindView(final ViewHolder ui, List<Object> payloads) {
			super.bindView(ui, payloads);

			ui.image.setImageDrawable(TransportrUtils.getTintedDrawable(getContext(), TransportrUtils.getDrawableForProduct(product)));
			ui.name.setText(TransportrUtils.productToString(getContext(), product));
			ui.checkBox.setChecked(isSelected());
			ui.layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = adapter.getAdapterPosition(ProductItem.this);
					adapter.toggleSelection(position);
					Set products = adapter.getSelectedItems();
					// if no products are selected, disable the ok-button
					if (products.size() == 0) {
						setOkEnabled(false);
					} else {
						setOkEnabled(true);
					}
				}
			});
		}

		@Override
		public ViewHolder getViewHolder(View view) {
			return new ProductItem.ViewHolder(view);
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			private ViewGroup layout;
			private ImageView image;
			private TextView name;
			private CheckBox checkBox;

			ViewHolder(View v) {
				super(v);

				layout = (ViewGroup) v;
				image = (ImageView) v.findViewById(R.id.productImage);
				name = (TextView) v.findViewById(R.id.productName);
				checkBox = (CheckBox) v.findViewById(R.id.productCheckBox);
			}
		}
	}
}
