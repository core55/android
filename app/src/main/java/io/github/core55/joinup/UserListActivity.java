package io.github.core55.joinup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {


    private ListView lv;
    private ArrayList userList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_list);
        lv = (ListView) findViewById(R.id.list);

        createUsers();
        UserAdapter adapter = new UserAdapter(this.getApplicationContext(), 0, userList);
        lv.setAdapter(adapter);

    }


    void createUsers() {
        String[] nicknames = {"Hussam", "Patrick", "Marcel", "Phillip", "Simone", "Dean", "Juan Luis", "Jiho", "Pepe", "Pablo"};
        String[] status = {"Hello, its me", "Maple syrup", "applestrudel", "biscuits please, not cookies", "Planet-tricky", "Baras", "biscuits please, not cookies", "biscuits please, not cookies", "biscuits please, not cookies", "biscuits please, not cookies"};
        int[] profilePictures = {R.drawable.emoji_1, R.drawable.emoji_2, R.drawable.emoji_3, R.drawable.emoji_4, R.drawable.emoji_1, R.drawable.emoji_2, R.drawable.emoji_2, R.drawable.emoji_1, R.drawable.emoji_3, R.drawable.emoji_4};
        for (int i = 0; i < nicknames.length; i++) {
            User u = new User(nicknames[i], status[i], profilePictures[i]);
            userList.add(u);
        }

    }

}
