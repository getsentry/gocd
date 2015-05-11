##########################GO-LICENSE-START################################
# Copyright 2015 ThoughtWorks, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##########################GO-LICENSE-END##################################

module ApiV1
  class AgentsRepresenter < BaseRepresenter

    link :self do |opts|
      opts[:url_builder].api_agents_url
    end

    link :doc do
      'http://www.go.cd/documentation/user/current/api/v1/agents.html'
    end

    collection :agents, embedded: true, exec_context: :decorator, decorator: AgentRepresenter

    def agents
      represented
    end
  end
end
