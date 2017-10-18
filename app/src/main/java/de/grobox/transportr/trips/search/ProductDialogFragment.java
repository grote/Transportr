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

package de.grobox.transportr.trips.search;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.settings.Preferences;
import de.schildbach.pte.dto.Product;

import static de.grobox.transportr.utils.TransportrUtils.getDrawableForProduct;
import static de.grobox.transportr.utils.TransportrUtils.productToString;

public class ProductDialogFragment extends DialogFragment {

	public static final String TAG = ProductDialogFragment.class.getSimpleName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	private DirectionsViewModel viewModel;
	private FastItemAdapter<ProductItem> adapter;
	private Button okButton;

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
		RecyclerView productsView = v.findViewById(R.id.productsView);
		productsView.setLayoutManager(new LinearLayoutManager(getContext()));
		adapter = new FastItemAdapter<>();
		adapter.withSelectable(true);
		productsView.setAdapter(adapter);
		for(Product product : Product.ALL) {
			adapter.add(new ProductItem(product));
		}

		// Get view model and observe products
		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(DirectionsViewModel.class);
		if (savedInstanceState == null) {
			viewModel.getProducts().observe(this, this::onProductsChanged);
		} else {
			adapter.withSavedInstanceState(savedInstanceState);
		}

		// OK Button
		okButton = v.findViewById(R.id.okButton);
		okButton.setOnClickListener(view -> {
			EnumSet<Product> products = getProductsFromItems(adapter.getSelectedItems());
			viewModel.setProducts(products);
			getDialog().cancel();
		});

		// Cancel Button
		Button cancelButton = v.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(view -> getDialog().cancel());
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		adapter.saveInstanceState(outState);
	}

	private void onProductsChanged(EnumSet<Product> products) {
		int i = 0;
		for (Product product : Product.ALL) {
			if (products.contains(product)) {
				adapter.select(i);
			}
			i++;
		}
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
			return R.layout.list_item_product;
		}

		@Override
		public void bindView(final ViewHolder ui, List<Object> payloads) {
			super.bindView(ui, payloads);

			ui.image.setImageResource(getDrawableForProduct(product));
			ui.name.setText(productToString(getContext(), product));
			ui.checkBox.setChecked(isSelected());
			ui.layout.setOnClickListener(v -> {
				int position = adapter.getAdapterPosition(ProductItem.this);
				adapter.toggleSelection(position);
				Set products = adapter.getSelectedItems();
				// if no products are selected, disable the ok-button
				if (products.size() == 0) {
					setOkEnabled(false);
				} else {
					setOkEnabled(true);
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
				image = v.findViewById(R.id.productImage);
				name = v.findViewById(R.id.productName);
				checkBox = v.findViewById(R.id.productCheckBox);
			}
		}
	}
}
