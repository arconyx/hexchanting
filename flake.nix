{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";

  outputs =
    { nixpkgs, ... }:
    let
      lib = nixpkgs.lib;
      # being a bit elaborate by passing pkgs as part of forAllSystems
      # instead of just generating it as needed
      packagesForSystem = system: nixpkgs.legacyPackages.${system};
      # f is a function system (str) -> packages (attrset) -> attrset
      forAllSystems =
        f: lib.genAttrs lib.systems.flakeExposed (system: f system (packagesForSystem system));
    in
    {
      devShells = forAllSystems (
        system: pkgs: {
          default =
            let
              libs = with pkgs; [
                libGL
                glfw3-minecraft
                pipewire
                flite
              ];
            in
            pkgs.mkShell {
              nativeBuildInputs = with pkgs; [
                jdk21

                # for hexdoc
                python312
                uv
              ];

              buildInputs = libs;

              env = {
                LD_LIBRARY_PATH = lib.makeLibraryPath libs;
                JAVA_HOME = pkgs.jdk21.home;
              };
            };
        }
      );
    };
}
