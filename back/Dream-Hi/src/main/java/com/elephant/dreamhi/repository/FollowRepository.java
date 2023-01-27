package com.elephant.dreamhi.repository;

import com.elephant.dreamhi.model.entity.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByActor_IdAndFollower_Id(Long actorId, Long followerId);

    Optional<Follow> findByProducer_IdAndFollower_Id(Long producerId, Long followerId);

    List<Follow> findAllByFollower_Id(Long userId);

    @Query("select f.producer.id from Follow f where f.follower.id=:userId and f.type='PRODUCER'")
    List<Long> findProducerIdByFollowerId(Long userId);

}