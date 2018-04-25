package com.capstonegyg.gyg.UI.NotificationsScreen;

import android.app.Notification;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.capstonegyg.gyg.UI.ViewGygScreen.ViewDetailedGyg.ViewDetailedGygActivity;
import com.capstonegyg.gyg.UI.ViewGygScreen.ViewGyg.ViewGygData;
import com.capstonegyg.gyg.UI.ViewGygScreen.ViewGyg.ViewGygViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Shawn on 4/24/18.
 */

public class NotificationFirebaseAdapter extends FirebaseRecyclerAdapter<NotificationsData, NotificationViewHolder> {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private FirebaseAuth firebaseAuth;

    private final int SOMEONE_ELSE = 0;
    private final int THIS_USER = 1;

    //Constructor
    public NotificationFirebaseAdapter(Class<NotificationsData> modelClass, int modelLayout, Class<NotificationViewHolder> viewHolderClass, Query query) {
        super(modelClass, modelLayout, viewHolderClass, query);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NotificationViewHolder viewHolder =  super.onCreateViewHolder(parent, viewType);

        viewHolder.setOnClickListener(new NotificationViewHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String gygKey = getRef(position).getKey();
                //Log.d("GYG_KEY", gygKey);
                //Intent i = new Intent(view.getContext(), ViewDetailedGygActivity.class);
                //Pass along the data
                //i.putExtra("GYG_KEY", gygKey);
                //Get context to start ViewDetailedGygActivity
                //view.getContext().startActivity(i);
            }
        });

        return viewHolder;
    }

    //Inject the model data into its respective viewholder/layout widget.
    @Override
    protected void populateViewHolder(final NotificationViewHolder viewHolder, final NotificationsData model, int position) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //If user not null and not current user
        if(user != null && !model.hitUserName.equals(user.getUid())) {

            //Get reference to user that posted
            usersReference = firebaseDatabase.getReference().child("users").child(model.gygName);
            //At child node get
            usersReference.child("display_name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Poster name
                    String name = (String) dataSnapshot.getValue();
                    //viewHolder.setGygPosterName(name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Errors
                }
            });

            //Set the gyg data
            viewHolder.setGygName(model.gygName);
            viewHolder.setHitUserName(model.hitUserName);

            viewHolder.setVisibility(View.VISIBLE);
        }
        //Unauthenticated or posted by user
        else {
            viewHolder.setVisibility(View.GONE);
        }
    }
}
