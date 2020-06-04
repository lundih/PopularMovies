package com.lundih.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.List;

class CustomAdapter extends ArrayAdapter<Movie> {
    private final Context context;

    CustomAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the movie object from the adapter at the position
        final Movie movie = getItem(position);

        // Inflate the layout if this is a new view object
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_item_layout, parent, false);

        ImageView imageViewMoviePoster = convertView.findViewById(R.id.imageViewMovieItemPoster);
        TextView textViewMovieTitle = convertView.findViewById(R.id.textViewMovieItemTitle);

        if (movie != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String imageQuality = sharedPreferences.getString(context.getString(R.string.key_image_resolution_shared_pref), context.getString(R.string.url_image_quality));

            Picasso.get().load(context.getString(R.string.url_base_image) + imageQuality + movie.getMoviePoster()).into(imageViewMoviePoster);
            imageViewMoviePoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
                    intent.putExtra(context.getString(R.string.intent_key_movie_id), movie.getMovieID());
                    intent.putExtra(context.getString(R.string.intent_key_movie_title), movie.getTitle());
                    context.startActivity(intent);
                }
            });
            textViewMovieTitle.setText(movie.getTitle());
        }

        return convertView;
    }
}