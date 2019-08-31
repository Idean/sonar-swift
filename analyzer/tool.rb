require_relative 'logging'
require_relative 'canfail'

# A base class for tool wrappers.
#
# Mainly defines a common interface + includes some useful modules.
class Tool
  include Logging
  include CanFail

  def self.command
  end

  def initialize(_options)
    validate_settings!
  end

  def run
  end

  protected
  def self.availability
	system("which #{self.command} 2>&1 > /dev/null")
  end

  protected
  def validate_settings!
  end
end