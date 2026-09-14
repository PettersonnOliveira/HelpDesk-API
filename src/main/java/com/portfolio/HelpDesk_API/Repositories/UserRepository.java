package com.portfolio.HelpDesk_API.Repositories;

import com.portfolio.HelpDesk_API.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>
{

}
