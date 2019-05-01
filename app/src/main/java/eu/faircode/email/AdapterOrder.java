package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private String clazz;
    private List<EntityOrder> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        ViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
        }

        private void bindTo(EntityOrder item) {
            tvTitle.setText(item.getSortTitle(context));
        }
    }

    AdapterOrder(Context context, LifecycleOwner owner, String clazz) {
        this.context = context;
        this.owner = owner;
        this.clazz = clazz;
        this.inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityOrder> items) {
        Log.i("Set sort items=" + items.size());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(items, new Comparator<EntityOrder>() {
            @Override
            public int compare(EntityOrder s1, EntityOrder s2) {
                int o = Integer.compare(s1.order == null ? -1 : s1.order, s2.order == null ? -1 : s2.order);
                if (o != 0)
                    return o;

                String name1 = s1.getSortTitle(context);
                String name2 = s2.getSortTitle(context);
                return collator.compare(name1, name2);
            }
        });

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(this.items, items), false);

        this.items = items;

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.i("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.i("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.i("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.i("Changed @" + position + " #" + count);
            }
        });
        diff.dispatchUpdatesTo(this);
    }

    private class DiffCallback extends DiffUtil.Callback {
        private List<EntityOrder> prev = new ArrayList<>();
        private List<EntityOrder> next = new ArrayList<>();

        DiffCallback(List<EntityOrder> prev, List<EntityOrder> next) {
            this.prev.addAll(prev);
            this.next.addAll(next);
        }

        @Override
        public int getOldListSize() {
            return prev.size();
        }

        @Override
        public int getNewListSize() {
            return next.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            EntityOrder s1 = prev.get(oldItemPosition);
            EntityOrder s2 = next.get(newItemPosition);
            return s1.getSortId().equals(s2.getSortId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityOrder s1 = prev.get(oldItemPosition);
            EntityOrder s2 = next.get(newItemPosition);
            return (Objects.equals(s1.order, s2.order));
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getSortId();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    List<EntityOrder> getItems() {
        return this.items;
    }

    void onMove(int from, int to) {
        if (from < 0 || from >= items.size() ||
                to < 0 || to >= items.size())
            return;

        if (from < to)
            for (int i = from; i < to; i++)
                Collections.swap(items, i, i + 1);
        else
            for (int i = from; i > to; i--)
                Collections.swap(items, i, i - 1);

        notifyItemMoved(from, to);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntityOrder item = items.get(position);
        holder.bindTo(item);
    }
}

