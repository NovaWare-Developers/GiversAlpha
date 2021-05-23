package pt.unl.fct.di.example.apdc2021.data;

public class EditProfileRepository {

    private static volatile EditProfileRepository instance;

    private UserDataSource dataSource;

    // private constructor : singleton access
    private EditProfileRepository(UserDataSource dataSource) {

        this.dataSource = dataSource;

    }

    public static EditProfileRepository getInstance(UserDataSource dataSource) {
        if (instance == null) {
            instance = new EditProfileRepository(dataSource);
        }
        return instance;
    }


    public Result<Void> editUser(String address, long bday, String gender, String phoneNum, String nacionality, String description, String tokenID) {
        // handle edit
        Result<Void> result = dataSource.editProfile(address, bday, gender, phoneNum, nacionality, description, tokenID);

        return result;
    }

}
