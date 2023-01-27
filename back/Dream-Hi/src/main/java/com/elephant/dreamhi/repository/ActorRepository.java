package com.elephant.dreamhi.repository;

import com.elephant.dreamhi.model.entity.ActorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends JpaRepository<ActorProfile, Long>, ActorRepositoryCustom {

}