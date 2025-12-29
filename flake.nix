{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";

  outputs =
    { nixpkgs, ... }:
    let
      # being a bit elaborate by passing pkgs as part of forAllSystems
      # instead of just generating it as needed
      packagesForSystem = system: nixpkgs.legacyPackages.${system};
      # f is a function system (str) -> packages (attrset) -> attrset
      forAllSystems =
        f:
        nixpkgs.lib.genAttrs nixpkgs.lib.systems.flakeExposed (system: f system (packagesForSystem system));
    in
    {
      devShells = forAllSystems (
        system: pkgs: {
          default = pkgs.mkShell {
            nativeBuildInputs = with pkgs; [
              stdenv.cc.cc.lib
              jdk21
              libglvnd
              xorg.libX11
              python312
              uv
            ];
            LD_LIBRARY_PATH = ''${pkgs.stdenv.cc.cc.lib}/lib:${pkgs.libglvnd}/lib:${pkgs.xorg.libX11}/lib'';
          };
        }
      );
    };
}
