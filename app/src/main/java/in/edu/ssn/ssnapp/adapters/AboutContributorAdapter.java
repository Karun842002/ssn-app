package in.edu.ssn.ssnapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.edu.ssn.ssnapp.R;
import in.edu.ssn.ssnapp.models.TeamDetails;

public class AboutContributorAdapter extends RecyclerView.Adapter<AboutContributorAdapter.ContributionViewHolder> implements View.OnClickListener{

    private ArrayList<TeamDetails> teamDetails;
    private Context context;

    public AboutContributorAdapter(Context context, ArrayList<TeamDetails> teamDetails) {
        this.context = context;
        this.teamDetails = teamDetails;
    }

    @NonNull
    @Override
    public AboutContributorAdapter.ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.contributor_item, parent, false);
        return new AboutContributorAdapter.ContributionViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull AboutContributorAdapter.ContributionViewHolder holder, int position) {
        TeamDetails drawer = (TeamDetails) teamDetails.get(position);

        holder.tv_name.setText(drawer.getName());
        holder.tv_position.setText(drawer.getPosition());
        holder.iv_dp.setImageResource(drawer.getDp());

        holder.iv_img1.setBackgroundResource(drawer.getType().get(0));
        holder.iv_img2.setBackgroundResource(drawer.getType().get(1));
        holder.iv_img3.setBackgroundResource(drawer.getType().get(2));

        holder.iv_img1.setTag(drawer.getUrl().get(0));
        holder.iv_img2.setTag(drawer.getUrl().get(1));
        holder.iv_img3.setTag(drawer.getUrl().get(2));

        holder.iv_img1.setOnClickListener(this);
        holder.iv_img2.setOnClickListener(this);
        holder.iv_img3.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return teamDetails.size();
    }

    public class ContributionViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_name, tv_position;
        public ImageView iv_dp, iv_img1, iv_img2, iv_img3;

        public ContributionViewHolder(View convertView) {
            super(convertView);

            tv_name = convertView.findViewById(R.id.tv_name);
            tv_position = convertView.findViewById(R.id.tv_position);
            iv_dp = convertView.findViewById(R.id.iv_dp);

            iv_img1 = convertView.findViewById(R.id.iv_img1);
            iv_img2 = convertView.findViewById(R.id.iv_img2);
            iv_img3 = convertView.findViewById(R.id.iv_img3);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_img1:
                if (v.getTag() != null) {
                    CharSequence seq = "dribbble";
                    if (!(v.getTag().toString().contains(seq)))
                        context.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", v.getTag().toString(), null)));
                    else
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(v.getTag().toString())));
                }
                break;
            case R.id.iv_img2:
                if (v.getTag() != null)
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(v.getTag().toString())));
                break;
            case R.id.iv_img3:
                if (v.getTag() != null)
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(v.getTag().toString())));
                break;
        }
    }
}