def student = 'imelnik'
(1..4).each {
    job("MNTLAB-$student-child" + it + '-build-job') {
        description 'Build and test the app.'
        configure {
          project ->
            project / 'properties' << 'hudson.plugins.copyartifact.CopyArtifactPermissionProperty' {
                'projectNameList'
            }
            project / 'properties' / 'hudson.plugins.copyartifact.CopyArtifactPermissionProperty' / 'projectNameList' {
                'string' '*'
            }
        }
        parameters {
            activeChoiceParam('BRANCH_NAME') {
                choiceType('SINGLE_SELECT')
                groovyScript {
                    script('''def gettags = ("git ls-remote -t -h https://github.com/MNT-Lab/dsl-task.git").execute()
return gettags.text.readLines().collect {
  it.split()[1].replaceAll('refs/heads/', '')
}''')
                }
            }
        }
        multiscm {
            git {
                remote {
                    url('git@github.com:MNT-Lab/dsl-task.git')
                    credentials('github-ssh-key')
                    branch('$BRANCH_NAME')
                }
            }
        }
        steps {
            shell('''
sh script.sh > output.txt
if [ -f *.groovy ]; then
tar -zcvf ${BRANCH_NAME}_dsl_script.tar.gz *.sh *.groovy
else
tar -zcvf ${BRANCH_NAME}_dsl_script.tar.gz *.sh
fi''')
        }
        publishers {
            archiveArtifacts {
                pattern('output.txt')
                pattern('${BRANCH_NAME}_dsl_script.tar.gz')
            }
        }
    }
}
job("MNTLAB-$student-main-build-job") {
    parameters {
        choiceParam('BRANCH_NAME', ['imelnik', 'main'])
        activeChoiceParam('BUILD_TRIGGER') {
            choiceType('CHECKBOX')
            groovyScript {
                script('''import jenkins.model.*
import hudson.model.*
def jobs = []
Jenkins.instance.getAllItems(AbstractItem.class).each  {
    if (it.name =~ /^MNTLAB-(.*)-child/) {
           jobs << it.fullName
    }
}
return jobs''')
            }
        }
    }
    steps {
        downstreamParameterized {
            trigger('$BUILD_TRIGGER') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
                }
            }
        }
    }
}
