package com.gamehub.dto;

public record GameSyncDTO(int received, int created, int updated, int skipped) {
}
