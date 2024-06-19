package com.tcmp.tiapi.program.repository;

import com.tcmp.tiapi.program.model.InterestTier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestTierRepository extends JpaRepository<InterestTier, Long> {
  @Query(
      value =
          """
          SELECT
            tier
          FROM
            InterestTier tier
          WHERE
            interest.map IS NOT NULL
              AND tier.number = 1
              AND interest.map.programType = 'B'
              AND interest.map.programId IN :programmeIds
              AND interest.map.counterPartyId IN :sellerIds""")
  List<InterestTier> findAllByProgrammeIdInAndSellerIdIn(
      Set<Long> programmeIds, Set<Long> sellerIds);

  @Query(
      value =
          """
          SELECT
            tier
          FROM
            InterestTier tier
          WHERE
            interest.map IS NULL
              AND tier.number = 1
              AND interest.map.programId IN :programmeIds""")
  List<InterestTier> findByProgrammeIdIn(Set<Long> programmeIds);

  @Query(
      value =
          """
          SELECT
            tier
          FROM
            InterestTier tier
          WHERE
            interest.map IS NOT NULL
              AND tier.number = 1
              AND interest.map.programType = 'B'
              AND interest.map.programId = :programmeId
              AND interest.map.counterPartyId = :sellerId""")
  Optional<InterestTier> findByProgrammeIdAndSellerId(Long programmeId, Long sellerId);

  @Query(
      value =
          """
          SELECT
            tier
          FROM
            InterestTier tier
          WHERE
            interest.map IS NULL
              AND tier.number = 1
              AND interest.map.programId = :programmeId""")
  Optional<InterestTier> findByProgrammeId(Long programmeId);
}
