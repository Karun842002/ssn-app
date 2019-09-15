package in.edu.ssn.ssnapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.hendraanggrian.appcompat.widget.SocialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import in.edu.ssn.ssnapp.adapters.ClubPostImageAdapter;
import in.edu.ssn.ssnapp.models.Club;
import in.edu.ssn.ssnapp.models.ClubPost;
import in.edu.ssn.ssnapp.utils.SharedPref;

public class ClubPageActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private RecyclerView feedsRV;
    public ImageView dp_pic, cover_pic, follow_iv, layout_dp_iv,dp_edit,cover_edit,name_edit,desc_edit,contact_edit;
    public TextView  followers_tv, layout_title;
    public EditText Club_name_tv,Club_desc_tv,contact_tv;
    private RelativeLayout layout_progress;
    private ShimmerFrameLayout shimmer_view;
    private FirestoreRecyclerAdapter adapter;
    RelativeLayout top_rl;
    CollapsingToolbarLayout collapsingToolbar;
    int clearance;
    Club club;

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.6f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private RelativeLayout mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference club_post_colref = db.collection("post_club");
    CollectionReference club_colref = db.collection("club");

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_page);
        initUI();
        bindActivity();

        mAppBarLayout.addOnOffsetChangedListener((AppBarLayout.OnOffsetChangedListener) this);

        startAlphaAnimation(mTitle, 0, View.INVISIBLE);

        setupFireStore();
    }

    void setupFireStore() {
        final TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .toUpperCase()
                .endConfig()
                .round();

        Query query = FirebaseFirestore.getInstance().collection("post_club").orderBy("time", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ClubPost> options = new FirestoreRecyclerOptions.Builder<ClubPost>().setQuery(query, new SnapshotParser<ClubPost>() {
            @NonNull
            @Override
            public ClubPost parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                shimmer_view.setVisibility(View.VISIBLE);

                final ClubPost post = new ClubPost();
                post.setId(snapshot.getString("id"));
                post.setCid(snapshot.getString("cid"));
                post.setAuthor(snapshot.getString("author"));
                post.setTitle(snapshot.getString("title"));
                post.setDescription(snapshot.getString("description"));
                post.setTime(snapshot.getTimestamp("time").toDate());
                try {
                    post.setLike((ArrayList<String>) snapshot.get("like"));
                    Log.i("app_test", "  like collected ");


                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("app_test", "  like not collected "+ e.toString());
                    post.setLike(null);
                }
                try {
                    post.setComment((ArrayList<String>) snapshot.get("comment"));
                    Log.i("app_test", "  comment collected ");


                }catch (Exception e){
                    Log.i("app_test", "  comment not collected "+ e.toString());
                    e.printStackTrace();
                    post.setComment(null);
                }


                ArrayList<String> images = (ArrayList<String>) snapshot.get("img_urls");
                if (images != null && images.size() > 0)
                    post.setImage_Urls(images);
                else
                    post.setImage_Urls(null);

                try {
                    ArrayList<Map<String, String>> files = (ArrayList<Map<String, String>>) snapshot.get("file_urls");
                    if (files != null && files.size() != 0) {
                        ArrayList<String> fileName = new ArrayList<>();
                        ArrayList<String> fileUrl = new ArrayList<>();

                        for (int i = 0; i < files.size(); i++) {
                            fileName.add(files.get(i).get("name"));
                            fileUrl.add(files.get(i).get("url"));
                        }
                        post.setFile_name(fileName);
                        post.setFile_urls(fileUrl);
                    } else {
                        post.setFile_name(null);
                        post.setFile_urls(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.log("stackTrace: " + Arrays.toString(e.getStackTrace()) + " \n Error: " + e.getMessage());
                    post.setFile_name(null);
                    post.setFile_urls(null);
                }
                return post;
            }
        }).build();

        adapter = new FirestoreRecyclerAdapter<ClubPost, FeedViewHolder>(options) {
            @Override
            public void onBindViewHolder(final FeedViewHolder holder, final int position, final ClubPost model) {
                if(clearance == 0){
                    name_edit.setVisibility(View.GONE);
                    desc_edit.setVisibility(View.GONE);
                    dp_edit.setVisibility(View.GONE);
                    cover_edit.setVisibility(View.GONE);
                    contact_edit.setVisibility(View.GONE);
                }else{
                    {
                        name_edit.setVisibility(View.VISIBLE);
                        desc_edit.setVisibility(View.VISIBLE);
                        dp_edit.setVisibility(View.VISIBLE);
                        cover_edit.setVisibility(View.VISIBLE);
                        contact_edit.setVisibility(View.VISIBLE);

                        name_edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Club_name_tv.setEnabled(true);
                            }
                        });
                        desc_edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Club_desc_tv.setEnabled(true);
                            }
                        });
                        contact_edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                contact_tv.setEnabled(true);
                            }
                        });
                    }
                }
                String author = "";
                String email = model.getAuthor();
                email = email.substring(0, email.indexOf("@"));
                for (int j = 0; j < email.length(); j++) {
                    if (Character.isDigit(email.charAt(j))) {
                        author = email.substring(0, j);
                        break;
                    }
                }
                if (author.isEmpty()) {
                    author = email;
                }
                holder.feed_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                            Intent intent = new Intent(getApplicationContext(), ClubPostDetailsActivity.class);
                            intent.putExtra("ClubPost", model);
                            intent.putExtra("Club", club);
                            startActivity(intent);

                    }
                });

                holder.tv_author.setText(author);
                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(model.getAuthor());
                TextDrawable ic1 = builder.build(String.valueOf(model.getAuthor().charAt(0)), color);
                holder.userImageIV.setImageDrawable(ic1);
                holder.tv_title.setText(model.getTitle());

                if (model.getLike().contains(SharedPref.getString(getApplicationContext(), "email"))) {
                    holder.like.setImageResource(R.drawable.blue_heart);

                } else {
                    holder.like.setImageResource(R.drawable.heart);
                }

                Date time = model.getTime();
                Date now = new Date();
                Long t = now.getTime() - time.getTime();
                String timer;

                if (t < 60000)
                    timer = Long.toString(t / 1000) + "s ago";
                else if (t < 3600000)
                    timer = Long.toString(t / 60000) + "m ago";
                else if (t < 86400000)
                    timer = Long.toString(t / 3600000) + "h ago";
                else if (t < 604800000)
                    timer = Long.toString(t / 86400000) + "d ago";
                else if (t < 2592000000L)
                    timer = Long.toString(t / 604800000) + "w ago";
                else if (t < 31536000000L)
                    timer = Long.toString(t / 2592000000L) + "M ago";
                else
                    timer = Long.toString(t / 31536000000L) + "y ago";

                holder.tv_time.setText(timer);

                if (model.getDescription().length() > 100) {
                    SpannableString ss = new SpannableString(model.getDescription().substring(0, 100) + "... see more");
                    ss.setSpan(new RelativeSizeSpan(0.9f), ss.length() - 12, ss.length(), 0);
                    ss.setSpan(new ForegroundColorSpan(Color.parseColor("#404040")), ss.length() - 12, ss.length(), 0);
                    holder.tv_description.setText(ss);
                } else
                    holder.tv_description.setText(model.getDescription().trim());

                if (model.getImage_Urls() != null && model.getImage_Urls().size() != 0) {
                    holder.viewPager.setVisibility(View.VISIBLE);

                    final ClubPostImageAdapter imageAdapter = new ClubPostImageAdapter(ClubPageActivity.this, model.getImage_Urls(), true, model, timer);
                    holder.viewPager.setAdapter(imageAdapter);

                    if (model.getImage_Urls().size() == 1) {
                        holder.tv_current_image.setVisibility(View.GONE);
                    } else {
                        holder.tv_current_image.setVisibility(View.VISIBLE);
                        holder.tv_current_image.setText(String.valueOf(1) + " / " + String.valueOf(model.getImage_Urls().size()));
                        holder.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                            }

                            @Override
                            public void onPageSelected(int pos) {
                                holder.tv_current_image.setText(String.valueOf(pos + 1) + " / " + String.valueOf(model.getImage_Urls().size()));
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {

                            }
                        });
                    }
                } else {
                    holder.viewPager.setVisibility(View.GONE);
                    holder.tv_current_image.setVisibility(View.GONE);
                }
                if (club.getFollowers().contains(SharedPref.getString(getApplicationContext(), "email"))) {
                    follow_iv.setImageResource(R.drawable.follow_blue);
                } else {
                    follow_iv.setImageResource(R.drawable.follow);
                }


                holder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!model.getLike().contains(SharedPref.getString(getApplicationContext(), "email"))) {
                            holder.like.setImageResource(R.drawable.blue_heart);
                            model.getLike().add(SharedPref.getString(getApplicationContext(), "email"));

                        } else {
                            holder.like.setImageResource(R.drawable.heart);
                            {
                                model.getLike().remove(SharedPref.getString(getApplicationContext(), "email"));
                            }
                        }
                        Map<String, Object> likes_details = new HashMap<>();
                        likes_details.put("like", model.getLike());
                        club_post_colref.document(model.getId()).set(likes_details, SetOptions.merge());

                    }
                });

                follow_iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (club.getFollowers().contains(SharedPref.getString(getApplicationContext(), "email"))) {
                            follow_iv.setImageResource(R.drawable.follow);
                            club.getFollowers().remove(SharedPref.getString(getApplicationContext(), "email"));

                        } else {
                            follow_iv.setImageResource(R.drawable.follow_blue);
                            club.getFollowers().add(SharedPref.getString(getApplicationContext(), "email"));
                        }
                        Map<String, Object> follower_det = new HashMap<>();
                        follower_det.put("followers", club.getFollowers());
                        club_colref.document(model.getId()).set(follower_det, SetOptions.merge());
                    }
                });


                Glide.with(ClubPageActivity.this).load(club.getDp_url()).placeholder(R.drawable.ic_user_white).into(dp_pic);
                Glide.with(ClubPageActivity.this).load(club.getCover_url()).placeholder(R.drawable.ic_user_white).into(cover_pic);
                Glide.with(ClubPageActivity.this).load(club.getDp_url()).placeholder(R.drawable.ic_user_white).into(layout_dp_iv);


                Club_name_tv.setText(club.getName());

                Club_desc_tv.setText(club.getDescription());
                contact_tv.setText(String.valueOf(club.getContact()));
                followers_tv.setText(String.valueOf(club.getFollowers().size()) + " followers");

                layout_title.setText(club.getName());
                if (model.getComment() != null ) {
                    if (model.getComment().size() != 0)
                        holder.comment_count.setText(String.valueOf(model.getComment().size()));
                    else
                        holder.comment_count.setText("");
                }
                else
                    holder.comment_count.setText("");
                holder.like_count.setText(String.valueOf(model.getLike().size()));


                layout_progress.setVisibility(View.GONE);
                shimmer_view.setVisibility(View.GONE);

            }

            @NonNull
            @Override
            public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(ClubPageActivity.this).inflate(R.layout.club_post_item, group, false);
                return new FeedViewHolder(view);
            }
        };

        feedsRV.setAdapter(adapter);
    }

    void initUI() {

        club = getIntent().getParcelableExtra("data");
        clearance = getIntent().getExtras().getInt("isHead");

        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        feedsRV = findViewById(R.id.feedsRV_cl);
        dp_pic = findViewById(R.id.dp_pic_iv);
        cover_pic = findViewById(R.id.cover_pic_iv);
        follow_iv = findViewById(R.id.follow_iv);
        contact_tv = findViewById(R.id.club_contact_tv);
        contact_tv.setEnabled(false);
        Club_name_tv = findViewById(R.id.club_name_tv);
        Club_name_tv.setEnabled(false);
        Club_desc_tv = findViewById(R.id.club_description_tv);
        Club_desc_tv.setEnabled(false);
        followers_tv = findViewById(R.id.followers_tv);
        mAppBarLayout = findViewById(R.id.main_appbarlayout);
        mToolbar = findViewById(R.id.main_toolbar);
        layout_dp_iv = findViewById(R.id.layout_dp_iv);
        layout_title = findViewById(R.id.layout_title_tv);
        dp_edit = findViewById(R.id.dp_pic_iv_edit);
        cover_edit= findViewById(R.id.cover_pic_iv_edit);
        name_edit= findViewById(R.id.club_name_tv_edit);
        desc_edit= findViewById(R.id.club_description_tv_edit);
        contact_edit= findViewById(R.id.club_contact_tv_edit);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ClubPageActivity.this);
        feedsRV.setLayoutManager(layoutManager);

        shimmer_view = findViewById(R.id.shimmer_view_cl);
        layout_progress = findViewById(R.id.layout_progress_cl);
    }

    /*********************************************************/

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_author, tv_club, tv_title, tv_time, tv_current_image, like_count, comment_count;
        public SocialTextView tv_description;
        public ImageView userImageIV, like, comment;
        public RelativeLayout feed_view;
        public ViewPager viewPager;

        public FeedViewHolder(View itemView) {
            super(itemView);

            tv_author = itemView.findViewById(R.id.tv_author_club);
            tv_title = itemView.findViewById(R.id.tv_title_club);
            tv_description = itemView.findViewById(R.id.tv_description_club);
            tv_time = itemView.findViewById(R.id.tv_time_club);
            tv_current_image = itemView.findViewById(R.id.currentImageTV_club);
            userImageIV = itemView.findViewById(R.id.userImageIV_club);
            feed_view = itemView.findViewById(R.id.feed_view_club);
            viewPager = itemView.findViewById(R.id.viewPager_club);
            like = itemView.findViewById(R.id.like_IV_club);
            comment = itemView.findViewById(R.id.comment_IV_club);
            like_count = itemView.findViewById(R.id.like_count_tv);
            comment_count = itemView.findViewById(R.id.comment_count_tv);
        }
    }

    /*********************************************************/

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void bindActivity() {
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mTitle = (TextView) findViewById(R.id.layout_title_tv);
        mTitleContainer = (RelativeLayout) findViewById(R.id.dp_cover_RL);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.main_appbarlayout);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        }
        else {
            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        }
        else {
            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
}
