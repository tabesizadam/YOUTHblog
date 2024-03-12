package com.pyjma.myapplication.Fragments;

import static com.unity3d.services.core.properties.ClientProperties.getApplicationContext;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pyjma.myapplication.Adapter;
import com.pyjma.myapplication.Model;
import com.pyjma.myapplication.R;
import com.pyjma.myapplication.databinding.FragmentHomeBinding;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;

import java.util.ArrayList;
import java.util.List;


public class Home extends Fragment implements IUnityAdsInitializationListener {


    FragmentHomeBinding binding;
    List<Model> list;
    private Adapter adapter;
    private Context context;
    private String unityGameID = "5573243";
    private Boolean testMode = true;
    String banner_ads_id = "bannerads";
    BannerView bannerView;

    FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();


    public Home(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false);

        setusearchview();
        setupRv();
        return binding.getRoot();

    }

    private void setusearchview() {
        binding.searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newText) {
        ArrayList<Model> filtered_list = new ArrayList<>();
        for(Model item:list){
            if (item.getTittle().toString().toLowerCase().contains(newText)){
                filtered_list.add(item);
            }
        }
        if (filtered_list.isEmpty()){
            //
        }
        else{
           // adapter.filter_list(filtered_list);
        }
    }

    private void setupRv() {
        list = new ArrayList<>();
        //  FirebaseFirestore.getInstance().collection("Blogs").orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
        //  @Override
        //  public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
        //     list.clear();
        //     for (DocumentSnapshot snapshot:value.getDocuments()){
        //         model = snapshot.toObject(Model.class);
        //          model.setId(snapshot.getId());
        //            list.add(model);
        ///         }
        //          adapter.notifyDataSetChanged();
        ///      }
        //   });
        firestoreDatabase.collection("Blogs").get().addOnSuccessListener(
                queryDocumentSnapshot -> {
                    Log.e("mehmet","Buraya Geldi Firebase");
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshot) {
                        String author = documentSnapshot.getString("author");
                        String date = documentSnapshot.getString("date");
                        String desc = documentSnapshot.getString("desc");
                        String img = documentSnapshot.getString("img");
                        String tittle = documentSnapshot.getString("tittle");
                        String id = documentSnapshot.getString("id");



                        Model model = new Model(tittle, desc, author, date, img,id);
                        list.add(model);



                    }
                    binding.rvBlogs.setHasFixedSize(true);
                    binding.rvBlogs.setLayoutManager(new StaggeredGridLayoutManager(1
                            ,StaggeredGridLayoutManager.VERTICAL));
                    adapter = new Adapter(getContext(),list);
                    binding.rvBlogs.setAdapter(adapter);



                }).addOnFailureListener(e -> {
                });

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_drawer);
        // Initialize the SDK:
        UnityAds.initialize(getApplicationContext(), unityGameID, testMode, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {

            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {

            }
        });
    }

    private void setContentView(int activityDrawer) {

    }

    @Override
    public void onInitializationComplete() {

    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {

    }




}