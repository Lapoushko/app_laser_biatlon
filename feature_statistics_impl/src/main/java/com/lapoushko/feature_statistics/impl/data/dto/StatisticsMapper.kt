package com.lapoushko.feature_statistics.impl.data.dto

import com.lapoushko.feature_statistics.api.domain.GameMode
import com.lapoushko.feature_statistics.api.domain.MatchStatistics
import com.lapoushko.feature_statistics.api.domain.Participant
import com.lapoushko.feature_statistics.api.domain.TargetStatus

fun MatchDataDto.toDomain(): MatchStatistics = MatchStatistics(
    gameMode = runCatching { GameMode.valueOf(metadata.gameMode.uppercase()) }.getOrDefault(GameMode.SINGLE),
    isInfiniteMode = metadata.isInfiniteMode,
    durationSeconds = metadata.matchDuration,
    remainingSeconds = metadata.remainingTime,
    startTimeIso = metadata.startTime,
    participants = results.map { (riflemanId, dto) -> dto.toDomain(riflemanId) },
    targets = targets.map { (targetId, dto) -> dto.toDomain(targetId) }
)

private fun PlayerDto.toDomain(riflemanId: String): Participant = Participant(
    riflemanId = riflemanId,
    fullName = fio,
    teamName = teamName,
    targetIds = targetIds,
    batteryCharge = vinCharge,
    magazinesLeft = numberMagazines,
    roundsLeft = numberRounds,
    shots = numberShots,
    hits = numberHits,
    targetHitPatterns = targetHits.mapKeys { it.key.toInt() }
)

private fun TargetDto.toDomain(targetId: String): TargetStatus = TargetStatus(
    id = targetId.toInt(),
    batteryCharge = charge,
    hitsCount = numberHits,
    players = listPlayer
)

fun MatchStatistics.toDto(): MatchDataDto = MatchDataDto(
    metadata = MetadataDto(
        gameMode = gameMode.name.lowercase(),
        startTime = startTimeIso,
        matchDuration = durationSeconds,
        isInfiniteMode = isInfiniteMode,
        remainingTime = remainingSeconds
    ),
    results = participants.associate { it.riflemanId to it.toDto() },
    targets = targets.associate { it.id.toString() to it.toDto() }
)

private fun Participant.toDto(): PlayerDto = PlayerDto(
    fio = fullName,
    teamName = teamName,
    targetIds = targetIds,
    vinCharge = batteryCharge,
    numberMagazines = magazinesLeft,
    numberRounds = roundsLeft,
    numberShots = shots,
    numberHits = hits,
    targetHits = targetHitPatterns.mapKeys { it.key.toString() }
)

private fun TargetStatus.toDto(): TargetDto = TargetDto(
    numberHits = hitsCount,
    charge = batteryCharge,
    listPlayer = players
)
