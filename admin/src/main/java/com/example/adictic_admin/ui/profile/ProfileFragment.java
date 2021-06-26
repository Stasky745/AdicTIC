package com.example.adictic_admin.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic_admin.App;
import com.example.adictic_admin.R;
import com.example.adictic_admin.entity.AdminProfile;
import com.example.adictic_admin.entity.WebLink;
import com.example.adictic_admin.rest.Api;
import com.example.adictic_admin.util.Constants;
import com.example.adictic_admin.util.Funcions;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment{

    private static final int AFEGIR_WEBLINK = 1;
    private static final int EDIT_WEBLINK = 2;
    private static final int AGAFAR_IMG = 3;
    private ArrayList<WebLink> webList;
    private RV_Adapter RVadapter;

    private final Context mCtx;
    private final AdminProfile adminProfile;

    private String nomOriginal;
    private String descOriginal;
    private String profOriginal;
    private ArrayList<WebLink> webListOriginal;
    private boolean imageChange = false;

    private TextInputEditText TIET_profileName;
    private TextInputEditText TIET_professio;
    private TextInputEditText TIET_desc;

    private ImageView IV_profilePic;

    private Api mService;

    private SharedPreferences sharedPreferences;

    public ProfileFragment(Context mCtx, AdminProfile adminProfile1) {
        this.adminProfile = adminProfile1;
        this.mCtx = mCtx;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == AFEGIR_WEBLINK){
                WebLink webLink = new WebLink();
                assert data != null;
                webLink.name = data.getExtras().getString("name");
                webLink.url = data.getExtras().getString("url");
                webList.add(webLink);
                RVadapter.notifyDataSetChanged();
            }
            else if(requestCode == EDIT_WEBLINK){
                assert data != null;
                int pos = data.getExtras().getInt("posicio");

                webList.get(pos).name = data.getExtras().getString("name");
                webList.get(pos).url = data.getExtras().getString("url");

                RVadapter.notifyDataSetChanged();
            }
            else if(requestCode == AGAFAR_IMG){
                try {
                    assert data != null;
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = requireActivity().getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageChange = true;
                    IV_profilePic.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this.getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_edit, container, false);

        mService = ((App) requireActivity().getApplication()).getAPI();

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getContext());

        webList = new ArrayList<>();

        TIET_profileName = root.findViewById(R.id.TIET_profileName);
        TIET_professio = root.findViewById(R.id.TIET_professio);
        TIET_desc = root.findViewById(R.id.TIET_desc);
        IV_profilePic = root.findViewById(R.id.IV_profilePic);

        setViews();
        agafarFoto(root);

        return root;
    }

    private void setViews() {
        TIET_profileName.setText(adminProfile.name);
        TIET_professio.setText(adminProfile.professio);
        TIET_desc.setText(adminProfile.description);
        webList = new ArrayList<>(adminProfile.webLinks);

        nomOriginal = adminProfile.name;
        descOriginal = adminProfile.description;
        profOriginal = adminProfile.professio;
        webListOriginal = new ArrayList<>(webList);
    }

    private boolean hiHaCanvis(){
        if(!Objects.requireNonNull(TIET_profileName.getText()).toString().equals(nomOriginal)) return true;
        else if(!Objects.requireNonNull(TIET_desc.getText()).toString().equals(descOriginal)) return true;
        else if(!Objects.requireNonNull(TIET_professio.getText()).toString().equals(profOriginal)) return true;
        //else if(imageChange) return true;
        return !webList.equals(webListOriginal);
    }

    private void agafarFoto(View root) {
        Funcions.setAdminPhoto(getContext(), sharedPreferences.getLong(Constants.SHARED_PREFS_ID_ADMIN,-1), IV_profilePic);
        setButtons(root);
        setRecyclerView(root);
    }

    private void setButtons(View root) {
        Button BT_accept = root.findViewById(R.id.BT_profileAccept);
        BT_accept.setOnClickListener(view -> {
            if(hiHaCanvis())
                postProfile();
            else if(imageChange)
                postImage();
            else
                previewProfile();
        });

        Button BT_addLink = root.findViewById(R.id.BT_addLink);
        BT_addLink.setOnClickListener(view -> {
            FragmentManager fm = getParentFragmentManager();

            SubmitWeblinkFragment dialog = new SubmitWeblinkFragment();

            dialog.setTargetFragment(this,AFEGIR_WEBLINK);
            dialog.show(fm,getString(R.string.new_weblink));
        });

        ImageView IV_profilePic = root.findViewById(R.id.IV_profilePic);
        IV_profilePic.setClickable(true);
        IV_profilePic.setOnClickListener(view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, AGAFAR_IMG);
        });
    }

    private void postProfile() {
        AdminProfile profile = new AdminProfile();
        profile.name = Objects.requireNonNull(TIET_profileName.getText()).toString();
        profile.professio = Objects.requireNonNull(TIET_professio.getText()).toString();
        profile.description = Objects.requireNonNull(TIET_desc.getText()).toString();
        profile.webLinks = webList;
        profile.oficina = adminProfile.oficina;

        Call<String> call = mService.postProfile(sharedPreferences.getLong(Constants.SHARED_PREFS_ID_ADMIN,-1),profile);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(mCtx, "S'ha pujat la informació al servidor.", Toast.LENGTH_SHORT).show();
                    if (imageChange)
                        postImage();
                    else
                        previewProfile();
                }
                else {
                    Toast.makeText(getActivity(), R.string.error_connect, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(getActivity(), R.string.error_connect, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postImage(){
        Bitmap bitmap = getBitmapFromDrawable(IV_profilePic.getDrawable());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS,100,stream);
        else
            bitmap.compress(Bitmap.CompressFormat.WEBP,100,stream);

        byte[] byteArray = stream.toByteArray();

        RequestBody requestFile =
                RequestBody.create(
                        byteArray,
                        MediaType.parse("image/webp")
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", "profilePic", requestFile);

        Call<String> call = mService.postPicture(sharedPreferences.getLong(Constants.SHARED_PREFS_ID_ADMIN,-1), body);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if(response.isSuccessful()){
                    Toast.makeText(mCtx, "S'ha pujat la imatge al servidor.", Toast.LENGTH_SHORT).show();
                    App.setAdminPic(IV_profilePic.getDrawable());
                    imageChange = false;
                    previewProfile();
                }
                else {
                    Toast.makeText(getActivity(), R.string.error_connect, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(getActivity(), R.string.error_connect, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void previewProfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(R.string.previsualitzar_titol);
        builder.setMessage(R.string.previsualitzar_desc);
        builder.setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
            startActivity(new Intent(getContext(), PreviewProfile.class));
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }

    private void setRecyclerView(View root){
        RecyclerView RV_webLinks = root.findViewById(R.id.RV_profileLinks);
        RV_webLinks.setLayoutManager(new LinearLayoutManager(getActivity()));
        RVadapter = new RV_Adapter(mCtx, webList);

        RV_webLinks.setAdapter(RVadapter);
    }

    public class RV_Adapter extends RecyclerView.Adapter<RV_Adapter.MyViewHolder>{
        Context mContext;
        LayoutInflater mInflater;
        List<WebLink> webList;

        public class MyViewHolder extends RecyclerView.ViewHolder{
            protected View mRootView;
            TextView TV_weblink;
            ImageView IV_delete, IV_edit;

            MyViewHolder(@NonNull View itemView){
                super(itemView);

                mRootView = itemView;

                TV_weblink = mRootView.findViewById(R.id.TV_weblink);
                IV_delete = mRootView.findViewById(R.id.IV_delete);
                IV_edit = mRootView.findViewById(R.id.IV_edit);
            }
        }

        RV_Adapter(Context context, List<WebLink> list){
            mContext = context;
            webList = list;
            mInflater = LayoutInflater.from(mContext);
        }

        @NonNull
        @Override
        public RV_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = mInflater.inflate(R.layout.weblink_item, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            holder.TV_weblink.setText(webList.get(position).name);

            setDeleteIV(holder,position);
            setEditIV(holder,position);
        }

        private void setEditIV(MyViewHolder holder, int position) {
            holder.IV_edit.setClickable(true);

            holder.IV_edit.setOnClickListener(view -> {
                FragmentManager fm = getParentFragmentManager();

                WebLink webLink = webList.get(position);

                SubmitWeblinkFragment dialog = new SubmitWeblinkFragment(webLink,position);

                dialog.setTargetFragment(ProfileFragment.this,EDIT_WEBLINK);
                dialog.show(fm,getString(R.string.weblink_edit));
            });
        }

        private void setDeleteIV(MyViewHolder holder, int position){
            holder.IV_delete.setClickable(true);

            holder.IV_delete.setOnClickListener(view -> new AlertDialog.Builder(mContext)
                    .setTitle(R.string.delete_weblink)
                    .setMessage(getString(R.string.confirm_delete_link,holder.TV_weblink.getText().toString()))

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(R.string.accept, (dialog, which) -> {
                        // Continue with delete operation
                        webList.remove(position);
                        notifyDataSetChanged();
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show());
        }

        @Override
        public int getItemViewType(int position) { return position; }

        @Override
        public int getItemCount() { return webList.size(); }
    }

    @NonNull
    private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }
}