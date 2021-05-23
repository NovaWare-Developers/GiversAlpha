package pt.unl.fct.di.example.apdc2021.data;

import pt.unl.fct.di.example.apdc2021.data.model.RegisteredUser;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class RegisterRepository {

    private static volatile RegisterRepository instance;

    private UserDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private RegisteredUser user = null;

    // private constructor : singleton access
    private RegisterRepository(UserDataSource dataSource) {

        this.dataSource = dataSource;

    }

    public static RegisterRepository getInstance(UserDataSource dataSource) {
        if (instance == null) {
            instance = new RegisterRepository(dataSource);
        }
        return instance;
    }

    public boolean isRegistered() {
        return user != null;
    }

    private void setRegisteredUser(RegisteredUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result<RegisteredUser> registerUser(String username, String password, String confirmation, String email, String name) {
        // handle register
        Result<RegisteredUser> result = dataSource.register(username, password, confirmation, email, name);
        if (result instanceof Result.Success) {
            setRegisteredUser(((Result.Success<RegisteredUser>) result).getData());
        }
        return result;
    }
}