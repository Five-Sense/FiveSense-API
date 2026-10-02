package com.fivesense.api.users.app;

import com.fivesense.api.users.domain.AppUser;
import com.fivesense.api.users.dto.UserDtos;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface UserMapper { UserDtos.Response toResponse(AppUser user); }
