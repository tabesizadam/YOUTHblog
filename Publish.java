package com.pyjma.myapplication.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pyjma.myapplication.databinding.FragmentPublishBinding;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Publish extends Fragment {

    private FragmentPublishBinding binding;
    private Uri filepath;
    Random random;

    public Publish() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPublishBinding.inflate(inflater, container, false);
        random = new Random();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selectImage();
        uploadData();
    }

    private void selectImage() {
        binding.view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Your Image"), 101);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            filepath = data.getData();
            binding.imgThumbnail.setVisibility(View.VISIBLE);
            binding.imgThumbnail.setImageURI(filepath);
            binding.view2.setVisibility(View.INVISIBLE);
            binding.bSelectImage.setVisibility(View.INVISIBLE);
        }
    }

    private void uploadData() {
        binding.btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.bTittle.getText().toString().isEmpty()) {
                    binding.bTittle.setError("Field is Required!!");
                    return;
                } else if (binding.bDesc.getText().toString().isEmpty()) {
                    binding.bDesc.setError("Field is Required!!");
                    return;
                } else if (binding.bAuthor.getText().toString().isEmpty()) {
                    binding.bAuthor.setError("Field is Required!!");
                    return;
                }

                ProgressDialog pd = new ProgressDialog(getContext());
                pd.setTitle("Uploading...");
                pd.setMessage("Yüklənir........");
                pd.setCancelable(false);
                pd.show();

                String title = binding.bTittle.getText().toString();
                String desc = binding.bDesc.getText().toString();
                String author = binding.bAuthor.getText().toString();

                if (filepath != null) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference reference = storage.getReference().child("images/" + filepath.getLastPathSegment());
                    reference.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        String fileUrl = task.getResult().toString();
                                        String date = (String) DateFormat.format("dd MMM", new Date());
                                        String deger = String.valueOf(random.nextInt(120000));
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("tittle", title);
                                        map.put("desc", desc);
                                        map.put("author", author);
                                        map.put("date", date);
                                        map.put("img", fileUrl);
                                        map.put("id",deger);

                                        FirebaseFirestore.getInstance().collection("Blogs").document(deger)
                                                .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            pd.dismiss();
                                                            Toast.makeText(getContext(), "Post Uploaded!!!", Toast.LENGTH_SHORT).show();

                                                            binding.imgThumbnail.setVisibility(View.INVISIBLE);
                                                            binding.view2.setVisibility(View.VISIBLE);
                                                            binding.bSelectImage.setVisibility(View.VISIBLE);
                                                            binding.bTittle.setText("");
                                                            binding.bDesc.setText("");
                                                            binding.bAuthor.setText("");
                                                        } else {
                                                            pd.dismiss();
                                                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void showSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Need Permission");
        builder.setMessage("");
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                getActivity().finish();
            }
        });
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
