package org.example.enums;

public enum ResultReason {
  ABORTED {
    @Override
    public String getMessage(String winnerUsername) {
      return "game aborted";
    }
  },
  FORFEIT {
    @Override
    public String getMessage(String winnerUsername) {
      return winnerUsername + " wins by forfeit.";
    }
  },
  TIMEOUT {
    @Override
    public String getMessage(String winnerUsername) {
      return winnerUsername + " wins on time.";
    }
  },
  CHECKMATE {
    @Override
    public String getMessage(String winnerUsername) {
      return winnerUsername + " wins by checkmate.";
    }
  },
  REPETITION {
    @Override
    public String getMessage(String winnerUsername) {
      return "draw by repetition.";
    }
  },
  INSUFFICIENT_MATERIAL {
    @Override
    public String getMessage(String winnerUsername) {
      return "draw by insufficient material.";
    }
  };

  public abstract String getMessage(String winnerUsername);
}
