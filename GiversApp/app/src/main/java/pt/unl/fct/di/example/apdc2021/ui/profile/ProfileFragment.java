package pt.unl.fct.di.example.apdc2021.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;
import pt.unl.fct.di.example.apdc2021.ExecutorApp;
import pt.unl.fct.di.example.apdc2021.R;
import pt.unl.fct.di.example.apdc2021.data.Result;
import pt.unl.fct.di.example.apdc2021.data.UserDataSource;
import pt.unl.fct.di.example.apdc2021.data.model.UserProfile;
import pt.unl.fct.di.example.apdc2021.ui.GetProfileResult;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    List<String> info = new ArrayList<>();
    private MutableLiveData<GetProfileResult> getProfileResult = new MutableLiveData<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        String tokenId = getArguments().getString("tokenId");
        String username = getArguments().getString("username");

        final TextView usernameTextView = root.findViewById(R.id.name);
        usernameTextView.setText(username);

        final CircleImageView profilePicture = root.findViewById(R.id.userphoto);

        UserDataSource dataSource = new UserDataSource();
        Executor executor = ((ExecutorApp) getActivity().getApplication()).getExecutorService();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Result<UserProfile> result = dataSource.getProfile(tokenId, username);
                if (result instanceof Result.Success) {
                    UserProfile data = ((Result.Success<UserProfile>) result).getData();
                    getProfileResult.postValue(new GetProfileResult(data));
                } else {
                    getProfileResult.postValue(new GetProfileResult(R.string.get_profile_failed));
                }
            }
        });

        getProfileResult.observe(getViewLifecycleOwner(), new Observer<GetProfileResult>() {
            @Override
            public void onChanged(@Nullable GetProfileResult getProfileResult) {
                if (getProfileResult == null) {
                    return;
                }
                if (getProfileResult.getError() != null) {
                    Toast.makeText(getActivity().getApplication().getApplicationContext(),
                            getProfileResult.getError(), Toast.LENGTH_SHORT).show();
                }
                if (getProfileResult.getSuccess() != null) {
                    UserProfile up = getProfileResult.getSuccess();
                    if(!up.getPhoto().getValue().equals("")){
                        String imageUrl = up.getPhoto().getValue();
                        Picasso.get().load(imageUrl).into(profilePicture);
                    }
                    if(!up.getAddress().getValue().equals(""))
                        info.add("Address: " + up.getAddress().getValue());
                    else
                        info.add("Address: ");
                    if(up.getDateOfBirth().getValue() != 0){
                        DateFormat simple = new SimpleDateFormat("dd/MM/yyyy");
                        Date result = new Date(up.getDateOfBirth().getValue());
                        info.add("Date of Birth: " + simple.format(result));
                    }else
                        info.add("Date of Birth: ");
                    if(up.getGender().getValue().equals("Male"))
                        info.add("Gender: Male");
                    else if(up.getGender().getValue().equals("Female"))
                        info.add("Gender: Female");
                    else if(up.getGender().getValue().equals("Other"))
                        info.add("Gender: Other");
                    else
                        info.add("Gender: ");
                    if(!up.getPhoneNr().getValue().equals(""))
                        info.add("Phone Number: " + up.getPhoneNr().getValue());
                    else
                        info.add("Phone Number: ");
                    if(!up.getNationality().getValue().equals(""))
                        info.add("Nationality: " + up.getNationality().getValue());
                    else
                        info.add("Nationality: ");
                    if(!up.getDescription().getValue().equals(""))
                        info.add("Description: " + up.getDescription().getValue());
                    else
                        info.add("Description: ");

                    //TODO:Get user's points
                    info.add("Points: 0");

                    ListView listView =  root.findViewById(R.id.userInfo);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_list_item_1, info);
                    listView.setAdapter(adapter);
                }
            }
        });

        return root;
    }
}