#!/bin/sh

#
# RHQ WebSphere Plug-in
# Copyright (C) 2012 Crossroads Bank for Social Security
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License, version 2, as
# published by the Free Software Foundation, and/or the GNU Lesser
# General Public License, version 2.1, also as published by the Free
# Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License and the GNU Lesser General Public License
# for more details.
#
# You should have received a copy of the GNU General Public License
# and the GNU Lesser General Public License along with this program;
# if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#

set -e

if [ $# -ne 2 ]; then
  echo "Usage: $(basename $0) <RHQ_AGENT_HOME> <WAS_HOME>"
  exit 1
fi
RHQ_AGENT_HOME=$1
WAS_HOME=$2
if [ ! -f $RHQ_AGENT_HOME/bin/rhq-agent.sh ]; then
  echo "$RHQ_AGENT_HOME doesn't appear to be an RHQ agent installation"
  exit 1
fi
if [ ! -f $WAS_HOME/runtimes/com.ibm.ws.admin.client_7.0.0.jar ]; then
  echo "$WAS_HOME doesn't appear to be a WAS 7.0 installation"
  exit 1
fi

sed -i -e "s|^#RHQ_AGENT_JAVA_HOME=.*|RHQ_AGENT_JAVA_HOME=$WAS_HOME/java|" $RHQ_AGENT_HOME/bin/rhq-agent-env.sh
ln -sf $WAS_HOME/runtimes/com.ibm.ws.admin.client_7.0.0.jar $RHQ_AGENT_HOME/lib/
ln -sf $WAS_HOME/plugins/com.ibm.ws.security.crypto.jar $RHQ_AGENT_HOME/lib/
ln -sf $WAS_HOME/plugins/com.ibm.ws.runtime.jar $RHQ_AGENT_HOME/lib/
echo "org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" > $RHQ_AGENT_HOME/conf/commons-logging.properties
DB2JCC=$(dirname $0)/db2jcc.jar
if [ -f $DB2JCC ]; then
  cp $DB2JCC $RHQ_AGENT_HOME/lib/
fi
