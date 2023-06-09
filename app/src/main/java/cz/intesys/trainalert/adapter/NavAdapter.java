package cz.intesys.trainalert.adapter;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cz.intesys.trainalert.R;
import cz.intesys.trainalert.databinding.DrawerItemBinding;
import cz.intesys.trainalert.entity.NavItem;

public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {
    private List<NavItem> mItems;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        /**
         * @param itemId string resource id of item title, which works like id of navigation item
         */
        void onCheckedItem(@StringRes int itemId);
    }

    public NavAdapter(OnItemClickListener listener) {
        mItems = new ArrayList<NavItem>();
        mItems.add(new NavItem(R.string.nav_pois, R.drawable.nav_pois));
        mItems.add(new NavItem(R.string.nav_categories, R.drawable.nav_categories));
        mItems.add(new NavItem(R.string.nav_settings, R.drawable.nav_settings));
        mItems.add(new NavItem(R.string.nav_profiles, R.drawable.nav_profiles));
        mItems.add(new NavItem(R.string.nav_manual, R.drawable.nav_manual));
        mItems.add(new NavItem(R.string.nav_logout, R.drawable.nav_logout));
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DrawerItemBinding binding = DrawerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mBinding.setData(mItems.get(position));
        holder.mBinding.getRoot().setOnClickListener((view) -> {
            mListener.onCheckedItem(mItems.get(position).getTitleRes());
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private DrawerItemBinding mBinding;

        public ViewHolder(DrawerItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
