package io.github.lllfjjj.tutor.practice;

import java.util.Optional;
import org.springframework.data.repository.Repository;

interface AttemptRepository extends Repository<Attempt, Long> {
	Attempt save(Attempt attempt);
	Optional<Attempt> findById(Long id);
}
