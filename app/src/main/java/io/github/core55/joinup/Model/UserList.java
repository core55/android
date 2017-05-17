package io.github.core55.joinup.Model;

import java.util.List;

import io.github.core55.joinup.Entity.User;

public class UserList {

    private _embedded _embedded;

    public UserList() {
    }

    public UserList(_embedded _embedded) {
        this();
        this._embedded = _embedded;
    }

    public List<User> getUsers() {
        return _embedded.getUsers();
    }

    public void setUsers(List<User> users) {
        this._embedded.setUsers(users);
    }

    public class _embedded {
        private List<User> users;

        public _embedded() {
        }

        public _embedded(List<User> users) {
            this();
            this.users = users;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }
    }
}
