package com.lundih.android.popularmovies;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<Review> reviews;

    ReviewAdapter(Context context, List<Review> reviews){
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        return new ReviewItem(layoutInflater.inflate(R.layout.review_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ReviewItem)holder).textViewContent.setText(reviews.get(position).getContent());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((ReviewItem)holder).textViewContent.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
        ((ReviewItem)holder).textViewAuthor.setText(reviews.get(position).getAuthor());
    }

    @Override
    public int getItemCount() {
        if (reviews != null) return reviews.size();
        else return 0;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    class ReviewItem extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewAuthor;

        public ReviewItem(@NonNull View itemView) {
            super(itemView);

            textViewContent = itemView.findViewById(R.id.textViewReviewContent);
            textViewAuthor = itemView.findViewById(R.id.textViewReviewAuthor);
        }
    }
}
