student = 'dmezhva'

// create main job

job('MNTLAB-' + student + '-main-build-job-test') {
    parameters {
        choiceParam('BRANCH_NAME', [student, 'main'])
    }

    parameters {
        activeChoiceParam('BUILD_TRIGGER') {
            choiceType('CHECKBOX')
            groovyScript {
                script('''def job_list = []
(1..4).each {
    job_list.add('MNTLAB-''' + student + '''-child" + it + "-build-job')
}
return job_list''')
            }
        }
    }
    steps {
        downstreamParameterized {
            trigger('$BUILDS_TRIGGER') {
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

// create child jobs

(1..4).each {
    job('MNTLAB-' + student + '-child' + it + '-build-job-test') {
        parameters {
            activeChoiceParam('BRANCH_NAME') {
                choiceType('SINGLE_SELECT')
                groovyScript {
                    script('''def gettags = ("git ls-remote -t -h https://github.com/MNT-Lab/dsl-task.git ").execute()
return gettags.text.readLines().collect { 
  it.split()[1].replaceAll('refs/heads/', '').replaceAll('refs/tags/', '').replaceAll("\\\\^\\\\{\\\\}", '')
}''')
                }
            }
        }
        scm {
            git {
                remote {
                    url('https://github.com/MNT-Lab/dsl-task')
                    branch('\$BRANCH_NAME')
                }
            }
        }
        steps {
            shell('''chmod +x ./script.sh
./script.sh > output.txt
tar -czf ${BRANCH_NAME}_dsl_script.tar.gz *.sh *.groovy''')
        }
        publishers {
            archiveArtifacts {
                pattern('*.txt')
                pattern('${BRANCH_NAME}_dsl_script.tar.gz')
                onlyIfSuccessful()
            }
        }
    }
} 