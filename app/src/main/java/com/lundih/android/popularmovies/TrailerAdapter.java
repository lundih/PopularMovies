package com.lundih.android.popularmovies;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class TrailerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<Trailer> trailers;
    String moviePoster;

    TrailerAdapter(Context context, List<Trailer> trailers, String moviePoster){
        this.context = context;
        this.trailers = trailers;
        this.moviePoster = moviePoster;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        return new TrailerItem(layoutInflater.inflate(R.layout.trailer_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (trailers != null) {
            ((TrailerItem)holder).textViewTitle.setText(trailers.get(position).getTitle());
            ((TrailerItem)holder).textViewSite.setText(trailers.get(position).getSite());
            ((TrailerItem)holder).textViewType.setText(trailers.get(position).getType());
            ((TrailerItem)holder).constraintLayoutTrailerItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check that the trailer is indeed from youtube
                    if ((trailers.get(position).getSite()).toLowerCase().equals(context.getString(R.string.value_youtube_for_comparison)) )
                        makeYoutubeIntent(position);
                    else
                        // If the trailer is not from youtube, run a search for it on the web using it's title
                        makeSearchIntent(position);
                }
            });
            Picasso.get().load(context.getString(R.string.url_base_image) +
                    PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_image_resolution_shared_pref), context.getString(R.string.url_image_quality_medium)) + moviePoster)
                    .transform(new BlurTransformation(context, 25, 2))
                    .into(((TrailerItem)holder).imageViewBackground);
        }
    }

    @Override
    public int getItemCount() {
        if (trailers != null) return trailers.size();
        else return 0;
    }

    private void makeYoutubeIntent(int position) {
        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_base_youtube_app) + trailers.get(position).getKey()));
        try {
            context.startActivity(youtubeIntent);
        } catch (ActivityNotFoundException e) {
            Intent otherAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_base_youtube_web) + trailers.get(position).getKey()));
            if (otherAppIntent.resolveActivity(context.getPackageManager()) != null)
                context.startActivity(otherAppIntent);
            else
                notifyNoAvailableAppToWatchTrailer();
        }
    }

    private void makeSearchIntent(int position){
        Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        searchIntent.putExtra(SearchManager.QUERY, trailers.get(position).getTitle());
        if (searchIntent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(searchIntent);
        else
            notifyNoAvailableAppToWatchTrailer();
    }

    private void notifyNoAvailableAppToWatchTrailer(){
        Toast.makeText(context, context.getString(R.string.message_no_app_to_watch_trailer), Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    class TrailerItem extends RecyclerView.ViewHolder{
        ConstraintLayout constraintLayoutTrailerItem;
        TextView textViewTitle;
        TextView textViewSite;
        TextView textViewType;
        ImageView imageViewBackground;

        public TrailerItem(@NonNull View itemView) {
            super(itemView);

            constraintLayoutTrailerItem = itemView.findViewById(R.id.constraintLayoutTrailerItem);
            textViewTitle = itemView.findViewById(R.id.textViewTrailerTitle);
            textViewSite = itemView.findViewById(R.id.textViewTrailerSite);
            textViewType = itemView.findViewById(R.id.textViewTrailerType);
            imageViewBackground = itemView.findViewById(R.id.imageViewMovieItemBackground);
        }
    }
}
