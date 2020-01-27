# Adds a fatal_error method that logs and exit.
# This module expects the Logging module included.
module CanFail
  def fatal_error(msg)
    logger.error(msg)
    exit(false)
  end
end
