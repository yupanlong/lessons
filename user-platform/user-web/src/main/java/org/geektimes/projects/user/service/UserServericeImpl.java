package org.geektimes.projects.user.service;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.sql.DBConnectionManager;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserServericeImpl implements UserService{
    private final Logger logger = Logger.getLogger(UserServericeImpl.class.getName());
    @Resource(name = "bean/EntityManager")
    private EntityManager entityManager;

    @Resource(name = "bean/Validator")
    private Validator validator;

    //private final UserRepository userRepository = new DatabaseUserRepository(new DBConnectionManager());
    @Override
    public boolean register(User user) {
        try {
            assert validator!=null;
            Set<ConstraintViolation<User>> validate = validator.validate(user);
            if (validate.size()!=0){
                for (ConstraintViolation<User> next : validate) {
                    logger.log(Level.INFO, next.getMessage());
                }
                return false;
            }
            entityManager.persist(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
        //return userRepository.save(user);
    }
    @Override
    public boolean deregister(User user) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User queryUserById(Long id) {
        return null;
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return null;
    }
}
