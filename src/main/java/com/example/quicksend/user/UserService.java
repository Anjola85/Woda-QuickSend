package com.example.quicksend.user;

import com.example.quicksend.config.BaseService;
import com.example.quicksend.util.dto.ServiceResult;
import com.example.quicksend.wallet.WalletDTO;
import com.example.quicksend.wallet.WalletService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements BaseService<UserDTO> {

    private final ModelMapper modelMapper;
    private final WalletService walletService;
    private ServiceResult<UserDTO> result;
    private final UserRepository userRepository;

    /**
     * Instances of the parameters of this constructor are created automatically
     * @param userRepository - this uses JPA to communicate with the DB
     * @param modelMapper - this helps with conversion of certain object types
     * @param userDTO - this creates an instance of an object to help transfer data between service and controller
     */
    @Autowired
    public UserService(UserRepository userRepository,
                       ModelMapper modelMapper,
                       ServiceResult<UserDTO> userDTO,
                       WalletService walletService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.modelMapper = modelMapper;
        this.result = userDTO;
    }

    public ServiceResult create(UserDTO user) {
        ServiceResult<Object> list = new ServiceResult<>();
        try {
            // check if user email exists
            if(userRepository.existsUserByEmail(user.getEmail())) {
                return list.setStatus(HttpStatus.CONFLICT)
                        .setMessage("email address %s already exists"
                                .formatted(user.getEmail()));
            } else if(userRepository.existsUserByPhoneNumber(user.getPhoneNumber())) {
                return list.setStatus(HttpStatus.CONFLICT)
                        .setMessage("phone number %s already exists"
                                .formatted(user.getPhoneNumber()));
            }
            // register user and save to db
            User newUser = modelMapper.map(user, User.class);
            newUser.setAge();
            UserDTO savedUser = modelMapper.map(userRepository.save(newUser), UserDTO.class); // convert newUser back to DTO
            WalletDTO userWallet = walletService.createWallet(savedUser.getId());
            List<Object> data = new ArrayList<>();
            data.add(savedUser);
            data.add(userWallet);
            return list.setStatus(HttpStatus.CREATED).setMessage("user successfully registered").setData(data);
        } catch (Exception e) {
            return list.setStatus(HttpStatus.INTERNAL_SERVER_ERROR).setMessage(e.getMessage());
        }
    }

    /**
     *
     * @param userID
     * @param updateRequest
     * @return updated user
     */
    public ServiceResult<UserDTO> update(Long userID, UserDTO updateRequest) {
        try {
            User user = userRepository.findUserById(userID).get();
            if(user == null) {
                return new ServiceResult<UserDTO>().setStatus(HttpStatus.NOT_FOUND).setMessage("user does not exist");
            }
            updateRequest.setId(userID); // In case this was not set, in order for BeanUtils to copy properly
            BeanUtils.copyProperties(updateRequest, user); // copy updated properties from updateRequest into user
            UserDTO savedUser = modelMapper.map(userRepository.save(user), UserDTO.class); // convert the user object returned by userRepository to UserDTO
            return result.setStatus(HttpStatus.OK).setMessage("user successfully saved").setData(savedUser);
        } catch (Exception e) {
            return new ServiceResult<UserDTO>().setStatus(HttpStatus.INTERNAL_SERVER_ERROR).setMessage(e.getMessage());
        }
    }

    /**
     *
     * @param id
     * @return
     */
    public ServiceResult<UserDTO> findById(Long id) {
        try {
            Optional<User> user = userRepository.findUserById(id);
            // check if user exists
            if(user.isEmpty()) {
                return new ServiceResult<UserDTO>().setStatus(HttpStatus.NOT_FOUND).setMessage("user does not exist");
            }
            // return retrieved user
            UserDTO returnedUser = modelMapper.map(user.get(), UserDTO.class);
            return result.setStatus(HttpStatus.OK).setMessage("user successfully fetched").setData(returnedUser);
        } catch (Exception e) {
            return new ServiceResult<UserDTO>().setStatus(HttpStatus.INTERNAL_SERVER_ERROR).setMessage(e.getMessage());
        }
    }

    /**
     *
     * @return
     */
    public ServiceResult<UserDTO> findAll() {
        try {
            List<UserDTO> userList = userRepository.findAll().stream()
                    .map(user -> modelMapper.map(user, UserDTO.class))
                    .collect(Collectors.toList());
            return result.setStatus(HttpStatus.OK).setMessage("users successfully fetched").setData(userList);
        } catch (Exception e) {
            return new ServiceResult<UserDTO>().setStatus(HttpStatus.INTERNAL_SERVER_ERROR).setMessage(e.getMessage());
        }
    }


}
