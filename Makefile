all: set clean compile

set:
	
	# Setting...
	@mkdir -p lib/
	@mkdir -p classes/

clean:
	
	# Cleaning...
	@rm -f -r lib/*
	@rm -f -r classes/*

compile:
	
	# Compiling the classes...
	@javac -Xlint -d classes/ src/BrainChat/*.java
	# Creating client exec jar...
	@cd classes/ \
		&& jar cvfe ../lib/Application.jar BrainChat.Application \
		BrainChat/Application* BrainChat/Linker* BrainChat/Client* \
		../assets
	# Creating server exec jar...
	@cd classes/ \
		&& jar cvfe ../lib/Server.jar BrainChat.Server \
		BrainChat/Server* BrainChat/Linker* BrainChat/Client*
