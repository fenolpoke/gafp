package edu.bluejack151.gafp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AvatarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AvatarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AvatarFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Firebase firebase;

    private OnFragmentInteractionListener mListener;

    public AvatarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AvatarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AvatarFragment newInstance(String param1, String param2) {
        AvatarFragment fragment = new AvatarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        firebase.setAndroidContext(getActivity().getApplicationContext());
        firebase = new Firebase("https://tpa-gap.firebaseio.com/");

        final Vector<Integer> userMoney = new Vector<Integer>();
        final Vector<String> userOwnedAvatar = new Vector<String>();

        firebase.child("users/" + firebase.getAuth().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Toast.makeText(getActivity().getApplicationContext(), dataSnapshot.getChildrenCount() + "", Toast.LENGTH_LONG).show();

                userMoney.add(Integer.parseInt(dataSnapshot.hasChild("money") ? dataSnapshot.child("money").getValue().toString() : "0"));
                DataSnapshot ownedAvatar = dataSnapshot.child("avatar");
                for (final DataSnapshot avatar : ownedAvatar.getChildren()) {
                    userOwnedAvatar.add(avatar.getKey().toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        firebase.child("shops/avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                Toast.makeText(getActivity().getApplicationContext(), dataSnapshot.getChildrenCount() + "", Toast.LENGTH_LONG).show();
                for (final DataSnapshot avatar : dataSnapshot.getChildren()) {
                    View layoutAvatar = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.avatar_layout, null);
                    TextView avTitle = (TextView) layoutAvatar.findViewById(R.id.avatarTitle);
                    TextView avPrice = (TextView) layoutAvatar.findViewById(R.id.avatarPrice);
                    ImageView avImage = (ImageView) layoutAvatar.findViewById(R.id.avatarImageShop);

                    final String title = avatar.child("name").getValue().toString();
                    final String price = avatar.child("price").getValue().toString();
                    final String imageName = avatar.getKey().toString();

                    int avaID = getResources().getIdentifier(imageName, "drawable", getActivity().getPackageName());
                    avImage.setImageResource(avaID);
                    avPrice.setText(price);
                    avTitle.setText(title);

                    Button btnPurchase = (Button) layoutAvatar.findViewById(R.id.btnPurchaseAvatar);
                    btnPurchase.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                       //     Toast.makeText(getActivity().getApplicationContext(), "Purchase " + v.getId(), Toast.LENGTH_LONG).show();

                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View alertPurchaseLayout = inflater.inflate(R.layout.popup_confirmation, null);
                            //Button purchase = (Button) alertPurchaseLayout.findViewById(R.id.btnPopUpPurchase);
                           // Button cancel = (Button) alertPurchaseLayout.findViewById(R.id.btnPopUpCancel);

                            final AlertDialog.Builder alertPurchase = new AlertDialog.Builder(getActivity());
                            alertPurchase.setView(alertPurchaseLayout);

                            int cancelBtnID = getResources().getIdentifier("btnPopUpCancel", "id", getActivity().getPackageName());
                            int purchaseBtnID = getResources().getIdentifier("btnPopUpPurchase", "id", getActivity().getPackageName());
                            alertPurchase.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Purchase canceled", Toast.LENGTH_SHORT).show();
                                }
                            });

                            alertPurchase.setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Integer money = userMoney.get(0);
                                    Integer purchasePrice = Integer.parseInt(price);
                                    if (money > purchasePrice) {
                                        money -= purchasePrice;
                                        Map<String, Object> updateMoney = new HashMap<String, Object>();
                                        updateMoney.put("money", money);
                                        firebase.child("users/" + firebase.getAuth().getUid()).updateChildren(updateMoney);

                                        //add
                                        Map<String, Object> addNewAvatar = new HashMap<String, Object>();
                                        addNewAvatar.put(imageName,true);
                                        firebase.child("users/" + firebase.getAuth().getUid()+"/avatar").updateChildren(addNewAvatar);


                                        Toast.makeText(getActivity().getApplicationContext(), "Thank you for purchasing", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getActivity().getApplicationContext(), "More money is required", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            alertPurchase.setTitle("Purchase Item");
                            AlertDialog dialog = alertPurchase.create();
                            dialog.show();
                        }
                    });

                    if (userOwnedAvatar.contains(imageName)) {
                        btnPurchase.setText("PURCHASED");
                        btnPurchase.setEnabled(false);
                        btnPurchase.setBackgroundColor(Color.GRAY);
                    } else {

                    }

                    ((LinearLayout) getActivity().findViewById(R.id.avatarLinearLayout)).addView(layoutAvatar);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_avatar, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
