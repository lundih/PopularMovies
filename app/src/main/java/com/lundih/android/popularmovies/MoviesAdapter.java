package com.lundih.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private List<Movie> movies;

    MoviesAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        return new MoviesAdapter.MovieItem(layoutInflater.inflate(R.layout.movie_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (movies != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String imageQuality = sharedPreferences.getString(context.getString(R.string.key_image_resolution_shared_pref), context.getString(R.string.url_image_quality_medium));

            Picasso.get().load(context.getString(R.string.url_base_image) + imageQuality + movies.get(position).getMoviePoster())
                    .placeholder(R.drawable.ic_image_place_holder)
                    .into(((MovieItem)holder).imageViewMoviePoster);
            ((MovieItem)holder).imageViewMoviePoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create an intent
                    Intent intent = new Intent(context, MovieDetailsActivity.class);
                    intent.putExtra(context.getString(R.string.intent_key_movie_id), movies.get(position).getMovieID());
                    intent.putExtra(context.getString(R.string.intent_key_movie_title), movies.get(position).getTitle());
                    context.startActivity(intent);
                }
            });
            ((MovieItem)holder).textViewMovieTitle.setText(movies.get(position).getTitle());
        }
    }

    @Override
    public int getItemCount() {
        if (movies != null) return movies.size();
        else return 0;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    class MovieItem extends RecyclerView.ViewHolder {
        ImageView imageViewMoviePoster;
        TextView textViewMovieTitle;

        MovieItem(@NonNull View movieItemView) {
            super(movieItemView);

            imageViewMoviePoster = movieItemView.findViewById(R.id.imageViewMovieItemPoster);
            textViewMovieTitle = movieItemView.findViewById(R.id.textViewMovieItemTitle);
        }
    }
}